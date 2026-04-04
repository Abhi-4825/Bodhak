package com.example.bodhakfrontend.Backend.Factory;


import com.example.bodhakfrontend.Backend.interfaces.ClassInfoBuilder;
import com.example.bodhakfrontend.Backend.interfaces.Parser;
import com.example.bodhakfrontend.Backend.ClassGraphBuilder;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.javaClassInfoBuilder;

import java.util.HashMap;
import java.util.Map;

public class ClassInfoBuilderFactory {

    private final Map<String, ClassInfoBuilder> classInfoBuilders=new HashMap<>();

    public ClassInfoBuilderFactory(ParserFactory parserFactory, ClassGraphBuilder classGraphBuilder){

        classInfoBuilders.put("java",new javaClassInfoBuilder(parserFactory, classGraphBuilder));

    }

    public ClassInfoBuilder getBuilder(String language){
             ClassInfoBuilder classInfoBuilder=classInfoBuilders.get(language.toLowerCase());
             if(classInfoBuilder==null){
                 throw new IllegalArgumentException("Unsupported language: " + language);
             }
             return classInfoBuilder;

    }

    public boolean support(String language){
        return classInfoBuilders.containsKey(language.toLowerCase());
    }








}
