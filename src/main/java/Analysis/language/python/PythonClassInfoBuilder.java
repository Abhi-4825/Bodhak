package Analysis.language.python;


import Analysis.ClassInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ConstructorInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodInfo;
import org.treesitter.TSNode;
import org.treesitter.TSPoint;

import java.io.File;
import java.lang.classfile.Superclass;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PythonClassInfoBuilder
{
    List<ClassInfo> classInfos= new ArrayList<ClassInfo>();
    public void buildClassInfo(TSNode classNode,String source)  {

        if(classNode.getType().equals("class_definition")){
            TSNode nameNode=classNode.getChildByFieldName("name");



        }

    }
    private ClassInfo getClassDetails(TSNode classNode, String source, String packageName, File sourceFile) {
        // ClassName
        String className=source.substring(
                classNode.getStartByte(),
                classNode.getEndByte()
        );


        // extract fields
        Set<String> fields=new HashSet<String>();
        extractFields(classNode,source,fields);

        // methods
        List<MethodInfo> methods=new ArrayList<MethodInfo>();

        //contructor
        List<ConstructorInfo> constructors=new ArrayList<ConstructorInfo>();


        // Annotations
        Set<String> annotations=new HashSet<>();
        extractClassAnnotations(classNode,source,annotations);

        // list of parent class
        List<String> parents=new ArrayList<>();
        getParentClasses(classNode,source,parents);


        // depends on
        Set<String> depends=new HashSet<>();

        // used by
        Set<String> usedBy=new HashSet<>();

        //circular dependencies group
        Set<Set<String>> circularDependsSets=new HashSet<>();


        // check if the class is enum, dataclass, ABC or simply class
        ClassInfo.Kind kind=detectPythonKind(parents,source,annotations);

        //is Abstract
        boolean isAbstract=false;
        if(inherits(parents,"ABC"))
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

        TSNode nameNode=classNode.getChildByFieldName("name");
        int beginLine=start.getRow()+1;
        int beginColumn=start.getColumn();





        return new ClassInfo(className,packageName,sourceFile,kind,fields,methods,constructors,annotations,
                depends,usedBy,circularDependsSets,isAbstract,isFinal,isPublic,
               linesOfCode,beginLine,beginColumn, )
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
        if(argList==null)return;
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
                            String fieldName=source.substring(assignment.getStartByte(),fieldNode.getEndByte());
                            fields.add(fieldName);
                        }
                    }
                }
            }
        }
    }






}
