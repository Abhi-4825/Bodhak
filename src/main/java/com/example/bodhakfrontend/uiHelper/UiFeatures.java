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

    public void openAndHighlight(String name,int beginLine,int column,File sourceFile) {

        Tab tab = openFile(sourceFile);
        highlight(tab,beginLine,column,name);
    }

    private void highlight(Tab tab, int beginLine, int ignoredColumn, String name) {

        CodeArea codeArea = (CodeArea) tab.getContent();

        int lineIndex = beginLine - 1;

        int totalLines =
                ((javafx.collections.ObservableList<?>) codeArea.getParagraphs()).size();

        if (lineIndex < 0 || lineIndex >= totalLines) {
            return;
        }

        String lineText = codeArea.getParagraph(lineIndex).getText();
        if (lineText == null || lineText.isEmpty()) {
            return;
        }

        int nameIndex = lineText.indexOf(name);
        if (nameIndex < 0) {
            codeArea.showParagraphAtTop(Math.max(0, lineIndex - 3));
            return;
        }

        int start = codeArea.getAbsolutePosition(lineIndex, nameIndex);
        int end   = start + name.length();

        codeArea.showParagraphAtTop(Math.max(0, lineIndex - 3));
        codeArea.moveTo(start);
        codeArea.selectRange(start, end);
    }

}
