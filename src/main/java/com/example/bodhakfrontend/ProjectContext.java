package com.example.bodhakfrontend;
import com.example.bodhakfrontend.Backend.Analysis.Engine.AnalysisEngine;

import com.example.bodhakfrontend.Backend.ClassGraphBuilder;
import com.example.bodhakfrontend.Backend.Factory.ParserFactory;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.*;
import com.example.bodhakfrontend.Backend.IncrementalPart.UpdateManager;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;
import com.example.bodhakfrontend.Backend.models.incrementalModel.ClassInfoViewModel;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Parser.javaParseCache;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class ProjectContext {
    public final javaParseCache cache;
    public final JavaFileParser javaFileParser;
    public final Parsermanager parsermanager;
    public final ClassNameResolver  classNameResolver;
    public final UpdateManager updateManager;
    public final List<Path> sourceRoots;
    public final Set<String> sourceClasses;
    public final MultiModuleSourceRootDetector multiModuleSourceRootDetector;
    public final ClassInfoViewModelBuilder classInfoViewModelBuilder;
    public final ProjectInfo projectInfo;
    public final Map<String, ClassInfoViewModel> vmMap;
    public final AnalysisEngine analysisEngine;
    public final ParserFactory parserFactory;
    public final ClassGraphBuilder classGraphBuilder;
    public ProjectContext(File projectFolder,
                          LanguageDetector detector) {
      this. multiModuleSourceRootDetector = new MultiModuleSourceRootDetector();
        this.sourceRoots = multiModuleSourceRootDetector.detectSourceRoots(projectFolder.toPath());
        this.cache = new javaParseCache(sourceRoots);
        this.javaFileParser = new JavaFileParser(cache);
        this.classNameResolver = new ClassNameResolver();

        this.parserFactory=new ParserFactory(sourceRoots);
        this.classGraphBuilder=new ClassGraphBuilder(parserFactory);


        this.parsermanager = new Parsermanager(detector, javaFileParser);



        this.analysisEngine=new AnalysisEngine(parserFactory,classGraphBuilder);

        this.sourceClasses = javaFileParser.getClassesfromSource(sourceRoots);
        this.classInfoViewModelBuilder=new ClassInfoViewModelBuilder(classGraphBuilder);
        this.updateManager=new UpdateManager(analysisEngine);

         analysisEngine.analyse(projectFolder.toPath());
        this.projectInfo=analysisEngine.getProjectInfo();
        List<ClassInfo> c=projectInfo.getClassInfos();
        this.vmMap=classInfoViewModelBuilder.initialBuild(projectInfo.getClassInfos());



    }
}

