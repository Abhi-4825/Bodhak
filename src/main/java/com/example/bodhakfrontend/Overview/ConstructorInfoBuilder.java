package com.example.bodhakfrontend.Overview;

import com.example.bodhakfrontend.Models.ConstructorInfo;
import com.example.bodhakfrontend.Models.MethodParameterInfo;
import com.example.bodhakfrontend.Models.ModifierKind;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.util.ParseCache;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructorInfoBuilder {
     private final JavaFileParser parser;
     private  final ParseCache cache;

    public ConstructorInfoBuilder(JavaFileParser parser, ParseCache cache) {
        this.parser = parser;
        this.cache = cache;
    }

    public Map<String, List<ConstructorInfo>> build(Path projectRoot){
        Map<String,List<ConstructorInfo>> map=new HashMap<>();
        try{
            Files.walk(projectRoot).filter(file->file.toString().endsWith(".java")).forEach(path->{

                File file=path.toFile();
                try{
                CompilationUnit cu = cache.get(path);
                if(cu==null)return;
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz->{

                    List<ConstructorInfo> constructorInfos=getConstructorInfo(clazz,file);
//                    for(ConstructorDeclaration cd:clazz.getConstructors()){
//                        List<MethodParameterInfo> param=new ArrayList<>();
//                        cd.getParameters().forEach(p->{
//                            param.add(new MethodParameterInfo(p.getNameAsString(),p.getTypeAsString()));
//                        });
//                        List<ModifierKind> modifiers=new ArrayList<>();
//                        cd.getModifiers().forEach(m->{
//                            modifiers.add(ModifierKind.from(m.getKeyword()));
//                        });
//                        var range=cd.getName().getRange().orElse(null);
//                        if(range==null){return;}
//                        int begin=range.begin.line;
//                        int end=range.end.line;
//                        int column=range.begin.column;
//
//                        constructorInfos.add(new ConstructorInfo(cd.getNameAsString(),param,modifiers,begin,end,column,file));
//
//                    }
                    map.put(clazz.getNameAsString(),constructorInfos);



                });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            });


        }catch(Exception e){
            e.printStackTrace();
        }

        return map;
    }
    public List<ConstructorInfo> getConstructorInfo(ClassOrInterfaceDeclaration clazz,File file){

        List<ConstructorInfo> constructorInfos=new ArrayList<>();
        for(ConstructorDeclaration cd:clazz.getConstructors()){
            List<MethodParameterInfo> param=new ArrayList<>();
            cd.getParameters().forEach(p->{
                param.add(new MethodParameterInfo(p.getNameAsString(),p.getTypeAsString()));
            });
            List<ModifierKind> modifiers=new ArrayList<>();
            cd.getModifiers().forEach(m->{
                modifiers.add(ModifierKind.from(m.getKeyword()));
            });
            var range=cd.getName().getRange().orElse(null);
            int begin=range.begin.line;
            int end=range.end.line;
            int column=range.begin.column;

            constructorInfos.add(new ConstructorInfo(cd.getNameAsString(),param,modifiers,begin,end,column,file));

        }
        return constructorInfos;
    }


}
