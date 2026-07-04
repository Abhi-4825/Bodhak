package com.example.bodhak.event.handler;

import com.example.bodhak.event.UiUpdateEvent;
import com.example.bodhak.event.domain.EditorCloseEvent;
import com.example.bodhak.event.domain.EditorReloadEvent;
import com.example.bodhak.ui.rightPanel.RightPanelTabManager;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles {@link EditorReloadEvent} and {@link EditorCloseEvent}.
 *
 * <ul>
 *   <li>Reload: re-reads the file from disk and replaces the {@link CodeArea}
 *       content, preserving the caret position where possible.</li>
 *   <li>Close: removes the editor tab and any associated overview tabs.</li>
 * </ul>
 *
 * <p>Runs on the JavaFX Application Thread.
 */
public final class EditorHandler implements UiUpdateHandler {

    private final TabPane codeTabPane;
    private final RightPanelTabManager rightPanel;

    public EditorHandler(TabPane codeTabPane, RightPanelTabManager rightPanel) {
        this.codeTabPane = codeTabPane;
        this.rightPanel  = rightPanel;
    }

    @Override
    public boolean canHandle(UiUpdateEvent event) {
        return event instanceof EditorReloadEvent || event instanceof EditorCloseEvent;
    }

    @Override
    public void apply(UiUpdateEvent event) {
        if (event instanceof EditorReloadEvent e) {
            reloadEditor(e.filePath());
        } else if (event instanceof EditorCloseEvent e) {
            closeEditor(e.filePath());
            rightPanel.closeOverviewTabs(e.filePath());
        }
    }

    // ── Reload ────────────────────────────────────────────────────────────────

    private void reloadEditor(Path path) {
        for (Tab tab : codeTabPane.getTabs()) {
            if (!(tab.getUserData() instanceof File file)) continue;
            if (!file.toPath().toAbsolutePath().normalize()
                    .equals(path.toAbsolutePath().normalize())) continue;

            Node content = tab.getContent();
            if (content instanceof CodeArea codeArea) {
                reloadCodeArea(codeArea, file);
            }
            break;
        }
    }

    private void reloadCodeArea(CodeArea codeArea, File file) {
        try {
            String newContent = Files.readString(file.toPath());
            int oldCaret = Math.min(codeArea.getCaretPosition(), newContent.length());
            codeArea.replaceText(newContent);
            // Restore caret on next FX pulse — after layout completes
            codeArea.moveTo(oldCaret);
        } catch (Exception ex) {
            codeArea.replaceText("// Error reloading file\n" + ex.getMessage());
        }
    }

    // ── Close ─────────────────────────────────────────────────────────────────

    private void closeEditor(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        codeTabPane.getTabs().removeIf(tab -> {
            if (tab.getUserData() instanceof File file) {
                return file.toPath().toAbsolutePath().normalize().equals(normalized);
            }
            return false;
        });
    }
}
