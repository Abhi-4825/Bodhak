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
import java.util.function.BiConsumer;

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
                          LanguageDetector detector, BiConsumer<Integer,Integer> progressCall) {

        int totalSteps=6;
        int current =0;
        // first detect source root
        this. multiModuleSourceRootDetector = new MultiModuleSourceRootDetector();
        this.sourceRoots = multiModuleSourceRootDetector.detectSourceRoots(projectFolder.toPath());
        progressCall.accept(++current, totalSteps);

        // we will change this part as we are making it support multi lang
        this.cache = new javaParseCache(sourceRoots);
        this.javaFileParser = new JavaFileParser(cache);
        this.classNameResolver = new ClassNameResolver();
        progressCall.accept(++current, totalSteps);

        this.parserFactory=new ParserFactory(sourceRoots);
        this.classGraphBuilder=new ClassGraphBuilder(parserFactory);
        this.parsermanager = new Parsermanager(detector, javaFileParser);
        progressCall.accept(++current, totalSteps);


        this.sourceClasses = javaFileParser.getClassesfromSource(sourceRoots);
        progressCall.accept(++current, totalSteps);
        this.classInfoViewModelBuilder=new ClassInfoViewModelBuilder(classGraphBuilder);
        this.analysisEngine=new AnalysisEngine(classInfoViewModelBuilder,parserFactory,classGraphBuilder);
        this.updateManager=new UpdateManager(analysisEngine);
        progressCall.accept(++current, totalSteps);

         analysisEngine.analyse(projectFolder.toPath());
        this.projectInfo=analysisEngine.getProjectInfo();
        progressCall.accept(++current, totalSteps);
        List<ClassInfo> c=projectInfo.getClassInfos();
        this.vmMap=classInfoViewModelBuilder.initialBuild(projectInfo.getClassInfos());
        progressCall.accept(++current, totalSteps);
    }
}

