package com.example.bodhakfrontend.Overview;

import com.example.bodhakfrontend.Models.ConstructorInfo;
import com.example.bodhakfrontend.Models.MethodParameterInfo;
import com.example.bodhakfrontend.Models.ModifierKind;
import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructorInfoBuilder {


    public Map<String, List<ConstructorInfo>> build(Path projectRoot){
        Map<String,List<ConstructorInfo>> map=new HashMap<>();
        try{
            Files.walk(projectRoot).filter(file->file.toString().endsWith(".java")).forEach(path->{

                File file=path.toFile();
                try{
                CompilationUnit cu = StaticJavaParser.parse(file);
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz->{

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
                        int begin=-1;
                        int end=-1;
                        if(cd.getRange().isPresent()){
                            begin=cd.getRange().get().begin.line;
                            end=cd.getRange().get().end.line;
                        }
                        constructorInfos.add(new ConstructorInfo(cd.getNameAsString(),param,modifiers,begin,end,file));

                    }
                    map.put(clazz.getNameAsString(),constructorInfos);



                });
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }


            });


        }catch(Exception e){
            e.printStackTrace();
        }

        return map;
    }


}
