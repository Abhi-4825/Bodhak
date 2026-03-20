package com.example.bodhakfrontend.Backend.Factory;


import com.example.bodhakfrontend.Backend.interfaces.ClassNameExtractor;

import com.example.bodhakfrontend.Backend.languages.JavaLanguage.utils.JavaClassNameExtractor;

import java.util.HashMap;
import java.util.Map;

public class ClassNameExtractorFactory {



    private final Map<String, ClassNameExtractor> classNameExtractors=new HashMap<>();
    public ClassNameExtractorFactory(ParserFactory parserFactory) {

        classNameExtractors.put("java",new JavaClassNameExtractor(parserFactory));
    }



    public ClassNameExtractor getClassNameExtractor(String language){
        ClassNameExtractor extractor=classNameExtractors.get(language.toLowerCase());
        if(extractor==null){
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return extractor;

    }







}
