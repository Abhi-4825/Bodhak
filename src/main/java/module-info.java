
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
    requires gs.core;
    requires gs.ui.javafx;

    exports com.example.bodhakfrontend.FrontEnd.MainScreen;
    opens com.example.bodhakfrontend.FrontEnd.MainScreen to javafx.fxml;

    opens com.example.bodhakfrontend.ui.Optimization to javafx.base;


    opens com.example.bodhakfrontend to javafx.fxml;
    exports com.example.bodhakfrontend;
    exports com.example.bodhakfrontend.Parser;
    opens com.example.bodhakfrontend.Parser to javafx.fxml;
    exports com.example.bodhakfrontend.Parser.javaParser;
    opens com.example.bodhakfrontend.Parser.javaParser to javafx.fxml;
    opens com.example.bodhakfrontend.Models to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.Backend.models.Class to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.Backend.models.Package to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.Backend.models.Project to com.fasterxml.jackson.databind;
    opens com.example.bodhakfrontend.Backend.IncrementalPart.Update to com.fasterxml.jackson.databind;
    exports com.example.bodhakfrontend.Nic;
    opens com.example.bodhakfrontend.Nic to javafx.fxml;
    exports com.example.bodhakfrontend.Nic.Model;
    opens com.example.bodhakfrontend.Nic.Model to javafx.fxml;
    exports com.example.bodhakfrontend.FrontEnd.uiHelper;
    opens com.example.bodhakfrontend.FrontEnd.uiHelper to javafx.fxml;


}