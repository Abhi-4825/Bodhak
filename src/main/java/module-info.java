
module com.example.bodhakfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.github.javaparser.core;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires jdk.compiler;
    requires javafx.graphics;
    requires javafx.base;
    requires com.github.javaparser.symbolsolver.core;
    requires org.javassist;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;

    requires org.checkerframework.checker.qual;


    opens com.example.bodhakfrontend.ui.Optimization to javafx.base;

    opens com.example.bodhakfrontend to javafx.fxml;
    exports com.example.bodhakfrontend;
    exports com.example.bodhakfrontend.Parser;
    opens com.example.bodhakfrontend.Parser to javafx.fxml;
    exports com.example.bodhakfrontend.Parser.javaParser;
    opens com.example.bodhakfrontend.Parser.javaParser to javafx.fxml;
    opens com.example.bodhakfrontend.Models to com.fasterxml.jackson.databind;
    exports com.example.bodhakfrontend.Builder;
    opens com.example.bodhakfrontend.Builder to javafx.fxml;
    opens com.example.bodhakfrontend.IncrementalPart.model.Class to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.IncrementalPart.model.Package to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.IncrementalPart.model.Project to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.IncrementalPart.Update to com.fasterxml.jackson.databind;
    exports com.example.bodhakfrontend.Nic;
    opens com.example.bodhakfrontend.Nic to javafx.fxml;


}