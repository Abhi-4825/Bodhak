package com.example.bodhakfrontend.uiHelper;

import com.example.bodhakfrontend.Models.DependencyNode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

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
    public void openAndHighlight(String name,int beginLine,File sourceFile) {

        Tab tab = openFile(sourceFile);
        highlight(tab,beginLine,name);
    }

    // ===============================
    // HIGHLIGHT LOGIC
    // ===============================
    private void highlight(Tab tab, int beginLine,String name) {

        CodeArea codeArea = (CodeArea) tab.getContent();

        int lineIndex = beginLine - 1;
        String className = name;
        int paragraphCount =
                ((java.util.List<?>) codeArea.getParagraphs()).size();
        if (lineIndex < 0 || lineIndex >=paragraphCount) {
            return;
        }

        codeArea.showParagraphAtTop(lineIndex);

        String lineText = codeArea.getParagraph(lineIndex).getText();
        int colIndex = lineText.indexOf(className);
        if (colIndex == -1) return;

        int start = codeArea.getAbsolutePosition(lineIndex, colIndex);
        int end = start + className.length();

        codeArea.moveTo(start);
        codeArea.selectRange(start, end);
    }
}
