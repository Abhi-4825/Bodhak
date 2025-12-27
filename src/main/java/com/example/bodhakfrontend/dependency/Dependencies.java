package com.example.bodhakfrontend.dependency;

import com.example.bodhakfrontend.Models.ClassDependencyInfo;
import com.example.bodhakfrontend.Models.ConstructorInfo;
import com.example.bodhakfrontend.Models.MethodsInfo;
import com.example.bodhakfrontend.Overview.ConstructorInfoBuilder;
import com.example.bodhakfrontend.Overview.MethodInfoBuilder;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.Models.DependencyNode;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Dependencies {
      private MultiModuleSourceRootDetector detector;
      private JavaFileParser javaFileParser;
      private CircularDependency circularDependency=new CircularDependency();
      private MethodInfoBuilder methodInfoBuilder=new MethodInfoBuilder();
      private ConstructorInfoBuilder constructorInfoBuilder=new ConstructorInfoBuilder();
  public Dependencies(MultiModuleSourceRootDetector detector,JavaFileParser javaFileParser) {
      this.detector=detector;
      this.javaFileParser=javaFileParser;
  }    

    private Map<String, Set<String>> getDependencies(File projectRoot)
    {
        List<Path> SourceRoot=detector.detectSourceRoots(projectRoot.toPath());
        Set<String> sourceClasses=javaFileParser.getClassesfromSource(SourceRoot);
      Map<String, Set<String>> depMap = new HashMap<>();
       try{
           Files.walk(projectRoot.toPath()).filter(path -> path.toString().endsWith(".java")).forEach(path -> {
               try {
                  Map<String,Set<String>> deps=javaFileParser.buildProjectDependencyGraph(path,sourceClasses);
                  deps.forEach((k,v)->{
                      depMap.computeIfAbsent(k,k2->new HashSet<>()).addAll(v);
                          }
                          );
               } catch (Exception e) {
                   throw new RuntimeException(e);
               }
           });
       }
       catch (Exception e)
       {e.printStackTrace();}


     return depMap;
    }

    public ClassDependencyInfo getProjectDependecyInfo(File projectRoot, Map<String, DependencyNode> classInfo)
    {
        Map<String,Set<String>> depMap=getDependencies(projectRoot);
        Set<Set<String>> circularDependencyGroup=circularDependency.findCircularDependency(depMap);
        Map<String,Set<String>> revDepMap=new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : depMap.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                revDepMap
                        .computeIfAbsent(to, k -> new HashSet<>())
                        .add(from);
            }
        }
        Map<String,List<MethodsInfo>> methods=methodInfoBuilder.build(projectRoot.toPath());
        Map<String,List<ConstructorInfo>> constructor=constructorInfoBuilder.build(projectRoot.toPath());

      return new ClassDependencyInfo(constructor,classInfo,depMap,revDepMap,circularDependencyGroup,methods);


    }





}



