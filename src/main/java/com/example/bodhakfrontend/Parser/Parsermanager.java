package com.example.bodhakfrontend.Parser;

import com.example.bodhakfrontend.LanguageDetector;
import com.example.bodhakfrontend.Parser.javaParser.JavaAstLabelProvider;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class Parsermanager {
 private LanguageDetector languageDetector=new LanguageDetector();
 private JavaFileParser javaFileParser=new JavaFileParser();
 private JavaAstLabelProvider javaLabelProvider=new JavaAstLabelProvider();
 public Object parseFile(File file) throws Exception {
     String fileType=languageDetector.detectFileType(file);
     switch (fileType) {
         case "Java":
             return javaFileParser.parseJava(file);

         default:
             throw new Exception(fileType +" not supported yet");
     }
 }
    // class-level dependencies
    public Map<String, Set<String>> parseClassWiseDependencies(File file, Set<String> sourceClasses) throws Exception {
        String type = languageDetector.detectFileType(file);

        switch (type) {
            case "Java":
                return javaFileParser.extractClassWiseDependencies(file,sourceClasses);
            default:
                throw new Exception("Dependencies not supported for: " + type);
        }
    }
    public AstLabelProvider<?> getLabelProvider(File file) throws Exception {
        String type = languageDetector.detectFileType(file);

        if (type.equals("Java")) {
            return javaLabelProvider;
        }

        throw new Exception("No AST label provider for: " + type);
    }




}
