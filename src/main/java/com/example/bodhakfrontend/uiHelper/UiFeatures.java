package com.example.bodhakfrontend.uiHelper;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import java.util.List;
import java.io.File;
import java.nio.file.Files;

public class UiFeatures {

    private final TabPane tabPane;

    public UiFeatures(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    // ===============================
    // UNIVERSAL FILE OPEN METHOD
    // ===============================
    public Tab openFile(File file) {


        for (Tab tab : tabPane.getTabs()) {
            if (file.equals(tab.getUserData())) {
                tabPane.getSelectionModel().select(tab);
                return tab;
            }
        }

        CodeArea codeArea = new CodeArea();
        codeArea.getStyleClass().add("code-area");
        codeArea.setEditable(true);

        try {
            codeArea.replaceText(Files.readString(file.toPath()));
        } catch (Exception e) {
            codeArea.replaceText("Failed to load file: " + e.getMessage());
        }

        codeArea.setParagraphGraphicFactory(
                LineNumberFactory.get(codeArea)
        );

        Tab tab = new Tab(file.getName());
        tab.setContent(codeArea);
        tab.setUserData(file);
        tab.setClosable(true);

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        return tab;
    }

    // ===============================
    // OPEN + HIGHLIGHT
    // ===============================
    public void openAndHighlight(String name,int beginLine,int column,File sourceFile) {

        Tab tab = openFile(sourceFile);
        highlight(tab,beginLine,column,name);
    }

    // ===============================
    // HIGHLIGHT LOGIC
    // ===============================
    private void highlight(Tab tab, int beginLine,int column,String name) {

        CodeArea codeArea = (CodeArea) tab.getContent();

        int paragraphIndex = beginLine - 1;
        int totalLines =
                ((javafx.collections.ObservableList<?>) codeArea.getParagraphs()).size();
        if (paragraphIndex < 0 || paragraphIndex >= totalLines) {
            return;
        }
        int absolutePos=codeArea.getAbsolutePosition(paragraphIndex,column-1);
        codeArea.showParagraphAtTop(Math.max(0,paragraphIndex-3));
        codeArea.moveTo(absolutePos);
        codeArea.selectRange(absolutePos,absolutePos+name.length());
    }
}
