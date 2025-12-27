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


    opens com.example.bodhakfrontend to javafx.fxml;
    exports com.example.bodhakfrontend;
    exports com.example.bodhakfrontend.Parser;
    opens com.example.bodhakfrontend.Parser to javafx.fxml;
    exports com.example.bodhakfrontend.Parser.javaParser;
    opens com.example.bodhakfrontend.Parser.javaParser to javafx.fxml;
    exports com.example.bodhakfrontend.projectAnalysis;
    opens com.example.bodhakfrontend.projectAnalysis to javafx.fxml;
}