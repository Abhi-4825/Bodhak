package com.example.anuviya.ui.helper;

import com.example.anuviya.ui.main.uiHelper.JavaSyntaxHighlighter;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.Subscription;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Collection;

public class UiFeatures {

    private final TabPane tabPane;

    public UiFeatures(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public Tab openFile(File file) {
        // If already open, just switch to it
        for (Tab tab : tabPane.getTabs()) {
            if (file.equals(tab.getUserData())) {
                tabPane.getSelectionModel().select(tab);
                return tab;
            }
        }

        CodeArea codeArea = new CodeArea();
        codeArea.getStyleClass().add("code-area");
        codeArea.setEditable(true);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // ── Syntax highlighting ───────────────────────────────────────────────
        // We use RichTextFX's reactive stream with a debounce so the highlight
        // computation never races with a concurrent text change.
        //
        // The key fix: we subscribe to 'plainTextChanges()' which fires AFTER
        // the text is fully committed. We then apply spans only if the document
        // length still matches what we computed against — preventing the
        // "(0, X) is not a valid range within (0, Y)" error completely.
        Subscription highlightSubscription = codeArea.plainTextChanges()
                .successionEnds(Duration.ofMillis(80))   // debounce rapid keystrokes
                .subscribe(change -> {
                    String current = codeArea.getText();
                    if (current.isEmpty()) return;

                    try {
                        StyleSpans<Collection<String>> spans =
                                JavaSyntaxHighlighter.computeHighlighting(current);

                        // Guard: only apply if the document hasn't changed again
                        // while the spans were being computed.
                        Platform.runLater(() -> {
                            if (codeArea.getLength() == current.length()) {
                                codeArea.setStyleSpans(0, spans);
                            }
                        });
                    } catch (Exception e) {
                        // Silently ignore highlight errors — content is always readable
                        System.out.println("[Highlight] Skipped: " + e.getMessage());
                    }
                });

        // Cancel subscription when the tab is closed to prevent leaks
        Tab tab = new Tab(file.getName());
        tab.setOnClosed(e -> highlightSubscription.unsubscribe());

        // ── Load file content ─────────────────────────────────────────────────
        try {
            String text = Files.readString(file.toPath());
            // replaceText fires the plainTextChanges subscriber for the initial highlight
            codeArea.replaceText(text);
        } catch (Exception e) {
            codeArea.replaceText("// Failed to load file: " + e.getMessage());
        }

        // Wrap in StackPane so AnalysisWorkspaceView can be overlaid per-tab
        javafx.scene.layout.StackPane tabStack = new javafx.scene.layout.StackPane(codeArea);
        tab.setContent(tabStack);
        tab.setUserData(file);
        tab.setClosable(true);

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        return tab;
    }

    public void openAndHighlight(String name, int beginLine, int column, File sourceFile) {
        Tab tab = openFile(sourceFile);
        // Defer highlight until layout is settled — openFile may have just
        // loaded the file and the CodeArea hasn't finished its layout pass yet.
        Platform.runLater(() -> highlight(tab, beginLine, column, name));
    }

    private void highlight(Tab tab, int beginLine, int ignoredColumn, String name) {
        // Tab content is a StackPane wrapping the CodeArea
        CodeArea codeArea = null;
        if (tab.getContent() instanceof javafx.scene.layout.StackPane sp) {
            codeArea = sp.getChildren().stream()
                    .filter(n -> n instanceof CodeArea)
                    .map(n -> (CodeArea) n)
                    .findFirst().orElse(null);
        } else if (tab.getContent() instanceof CodeArea ca) {
            codeArea = ca; // fallback for legacy tabs
        }
        if (codeArea == null) return;

        // Entity positions are 1-indexed from the analysis engine
        int lineIndex = Math.max(0, beginLine - 1);

        int totalLines = codeArea.getParagraphs().size();
        if (lineIndex >= totalLines) return;

        String lineText = codeArea.getParagraph(lineIndex).getText();
        if (lineText == null || lineText.isEmpty()) {
            codeArea.showParagraphAtTop(Math.max(0, lineIndex - 3));
            return;
        }

        int nameIndex = lineText.indexOf(name);

        if (nameIndex < 0) {
            // Fallback: just scroll to the line
            codeArea.showParagraphAtTop(Math.max(0, lineIndex - 3));
            return;
        }

        int start = codeArea.getAbsolutePosition(lineIndex, nameIndex);
        int end   = start + name.length();

        // Clamp to actual document length to be safe
        int textLength = codeArea.getLength();
        start = Math.max(0, Math.min(start, textLength));
        end   = Math.max(0, Math.min(end,   textLength));

        if (start >= end) return;

        codeArea.showParagraphAtTop(Math.max(0, lineIndex - 3));
        codeArea.requestFocus();
        codeArea.selectRange(start, end);
    }
}
