
module com.example.bodhakfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.github.javaparser.core;
    requires org.fxmisc.richtext;
    requires jdk.compiler;
    requires javafx.graphics;
    requires javafx.base;
    requires com.github.javaparser.symbolsolver.core;
    requires org.javassist;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires com.example.bodhakfrontend;

    opens com.example.bodhakfrontend to javafx.fxml;
    exports com.example.bodhakfrontend;
    exports com.example.bodhakfrontend.Parser;
    opens com.example.bodhakfrontend.Parser to javafx.fxml;
    exports com.example.bodhakfrontend.Parser.javaParser;
    opens com.example.bodhakfrontend.Parser.javaParser to javafx.fxml;
    exports com.example.bodhakfrontend.projectAnalysis;
    opens com.example.bodhakfrontend.projectAnalysis to javafx.fxml;
    exports com.example.bodhakfrontend.projectAnalysis.warning;
    opens com.example.bodhakfrontend.projectAnalysis.warning to javafx.fxml;
    opens com.example.bodhakfrontend.Models to com.fasterxml.jackson.databind;
    exports com.example.bodhakfrontend.Builder;
    opens com.example.bodhakfrontend.Builder to javafx.fxml;
    opens com.example.bodhakfrontend.Models.PackageAnalysis to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.Models.Incremental to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.IncrementalPart.model.Package to com.fasterxml.jackson.databind;
}