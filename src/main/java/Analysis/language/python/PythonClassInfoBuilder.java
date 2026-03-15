package Analysis.language.python;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassRole;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ConstructorInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodInfo;
import org.treesitter.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PythonClassInfoBuilder
{   private final PythonParseCache cache;
    private final PythonClassDependencyGraphBuilder dependencyGraphBuilder ;
    public PythonClassInfoBuilder(PythonParseCache cache, PythonClassDependencyGraphBuilder dependencyGraphBuilder){
        this.cache = cache;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
    }


    List<ClassInfo> classInfos= new ArrayList<ClassInfo>();
    public List<ClassInfo> buildClassInfos(Set<Path> pythonFiles) throws Exception {
        for (Path path : pythonFiles) {
            TSTree tree=cache.get(path);
            if (tree == null) {continue;}
            String source = Files.readString(path);
            TSNode root = tree.getRootNode();
            walk(root, source, path);
        }

        return classInfos;
    }
    private void walk(TSNode node, String source, Path file) {

        if (node.getType().equals("class_definition")) {

            ClassInfo info = getClassDetails(node, source, file);

            classInfos.add(info);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i), source, file);
        }
    }






            private ClassInfo getClassDetails(TSNode classNode, String source, Path sourceFile) {
        // ClassName
        TSNode nameNode=classNode.getChildByFieldName("name");
        String className=source.substring(
                nameNode.getStartByte(),
                nameNode.getEndByte()
        );
        // packageName
                String packageName = sourceFile.getParent() != null ? sourceFile.getParent().toString() : "";

        // extract fields
        Set<String> fields=new HashSet<String>();
        extractFields(classNode,source,fields);

        // methods
        PythonMethodInfoBuilder methodBuilder = new PythonMethodInfoBuilder();
        methodBuilder.build(classNode,source,sourceFile);
        List<MethodInfo> methods= methodBuilder.getMethods();

        //contructor
        PythonConstructorInfoBuilder constructorBuilder = new PythonConstructorInfoBuilder();
        constructorBuilder.build(classNode,source,sourceFile);
        List<ConstructorInfo> constructors=constructorBuilder.getConstructors();



        // Annotations
        Set<String> annotations=new HashSet<>();
        extractClassAnnotations(classNode,source,annotations);

        // list of parent class
        List<String> parents=new ArrayList<>();
        getParentClasses(classNode,source,parents);


        // depends on
                Set<String> depends = dependencyGraphBuilder
                        .getClassDependenciesToPath()
                        .getOrDefault(sourceFile, new HashMap<>())
                        .getOrDefault(className, new HashSet<>());

        // used by
                Set<String> usedBy = dependencyGraphBuilder
                        .getReverseClassDependencies()
                        .getOrDefault(className, new HashSet<>());

        //circular dependencies group
                Set<Set<String>> allCircularDependsSets =
                        dependencyGraphBuilder.getClassDependenciesGroups();
        Set<Set<String>> circularDependsSets=new HashSet<>();
                for (Set<String> cycle : allCircularDependsSets) {
                    if (cycle.contains(className)) {
                        circularDependsSets.add(cycle);
                    }
                }


        // check if the class is enum, dataclass, ABC or simply class
        ClassInfo.Kind kind=detectPythonKind(parents,source,annotations);

        //is Abstract
        boolean isAbstract=false;
        if(inherits(parents,"ABC") || inherits(parents,"ABCMeta"))
            isAbstract=true;
        // add method annotation for abstraction part

        boolean isFinal=false;
        if(annotations.contains("final"))
            isFinal=true;

//      isPublic
        boolean isPublic=!className.startsWith("_");


        TSPoint start=classNode.getStartPoint();
        TSPoint end=classNode.getEndPoint();

        int linesOfCode=end.getRow()-start.getRow()+1;

        TSPoint nameStart=nameNode.getStartPoint();
        int beginLine=nameStart.getRow()+1;
        int beginColumn=nameStart.getColumn();

        // ClassRole

        Set<ClassRole> role =PythonClassRoleDetector.detectRoles(classNode,className,source);

        return new ClassInfo(className,packageName,sourceFile.toFile(),kind,fields,methods,constructors,annotations,
                depends,usedBy,circularDependsSets,isAbstract,isFinal,isPublic,
               linesOfCode,beginLine,beginColumn, role);
    }

    private ClassInfo.Kind detectPythonKind(List<String> superclasses, String source,Set<String> annotations) {
        if(inherits(superclasses,"Enum"))
            return ClassInfo.Kind.ENUM;
        if(inherits(superclasses,"ABC"))
            return ClassInfo.Kind.ABC;
        if(hasDecorator(annotations,"dataclass"))
            return ClassInfo.Kind.DATACLASS;
        return ClassInfo.Kind.CLASS;
    }

    private boolean inherits(List<String> superclasses,String name) {
        if(superclasses==null || superclasses.isEmpty()) return false;
        return superclasses.contains(name);
    }
    private void  getParentClasses(TSNode classNode,String source,List<String> superClass) {
        TSNode argList=classNode.getChildByFieldName("superclasses");
        if(argList==null) argList=classNode.getChildByFieldName("arguments");
        for(int i=0;i<argList.getChildCount();i++){
            TSNode child=argList.getChild(i);
            if(child.getType().equals("identifier")){
                String parent=source.substring(child.getStartByte(),child.getEndByte());
                superClass.add(parent);
            }
        }
    }



    private boolean hasDecorator(Set<String> annotations,String decoratorName) {
       if(annotations==null|| annotations.isEmpty()) return false;
       return annotations.contains(decoratorName);
    }


    private void extractClassAnnotations(TSNode classNode,String source,Set<String> annotations) {
        TSNode parent=classNode.getParent();
        if(parent==null)return;
        if(!parent.getType().equals("decorated_definition")){
            return;
        }
        for(int i=0;i<parent.getChildCount();i++){
            TSNode child=parent.getChild(i);
            if(child.getType().equals("decorator")){
                TSNode decorator=child.getChild(0);
                String name=source.substring(decorator.getStartByte(),decorator.getEndByte());
                annotations.add(name);
            }
        }

    }


    // extract fields
    private void extractFields(TSNode classNode, String source, Set<String> fields){
        TSNode body=classNode.getChildByFieldName("body");
        if(body==null)return;
        for(int i=0;i<body.getChildCount();i++){
            TSNode child=body.getChild(i);
            if(child.getType().equals("assignment")){
                TSNode assignment=child.getChild(0);
                if(assignment.getType().equals("identifier")){

                    String name=source.substring(assignment.getStartByte(),assignment.getEndByte());
                    fields.add(name);

                }

            }
            if(child.getType().equals("function_definition")){
                TSNode methodBody=child.getChildByFieldName("body");
                if(methodBody==null)return;
                for (int j=0;j<methodBody.getChildCount();j++){
                    TSNode stmt=methodBody.getChild(j);
                    if(stmt.getType().equals("assignment")){
                        TSNode assignment=stmt.getChild(0);
                        if(assignment.getType().equals("attribute")){
                            TSNode fieldNode=assignment.getChild(1);
                            String fieldName=source.substring(fieldNode.getStartByte(),fieldNode.getEndByte());
                            fields.add(fieldName);
                        }
                    }
                }
            }
        }
    }






}
