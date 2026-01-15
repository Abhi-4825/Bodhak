//package com.example.bodhakfrontend.Builder;
//
//import com.example.bodhakfrontend.Models.MethodCallInfo;
//
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.expr.MethodCallExpr;
//import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
//import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
//import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
//import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumDeclaration;
//import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration;
//import com.github.javaparser.symbolsolver.javassistmodel.JavassistClassDeclaration;
//import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MethodCallInfoBuilder {
//
//   public List<MethodCallInfo> build(MethodDeclaration method, String currentClassFQN){
//
//       List<MethodCallInfo> methodCallInfoList = new ArrayList<>();
//       method.findAll(MethodCallExpr.class).forEach(methodCallExpr -> {
//           try{
//               ResolvedMethodDeclaration resolved=methodCallExpr.resolve();
//               ResolvedReferenceTypeDeclaration owner=resolved.declaringType();
//               MethodCallInfo.CallType type=classify(owner,currentClassFQN);
//               methodCallInfoList.add(new MethodCallInfo(
//                       resolved.getName(),
//                       currentClassFQN,
//                       owner.getQualifiedName(),
//                       type
//               ));
//
//
//
//
//           } catch (Exception ignored) {
//
//           }
//
//
//
//
//       });
//
//
//       return methodCallInfoList;
//   }
//
//    private MethodCallInfo.CallType classify(ResolvedReferenceTypeDeclaration owner, String currentClassFQN) {
//       if(owner.getQualifiedName().equals(currentClassFQN)){
//           return MethodCallInfo.CallType.INTERNAL;
//       }
//        if (owner instanceof JavaParserClassDeclaration ||
//                owner instanceof JavaParserEnumDeclaration ||
//                owner instanceof JavaParserInterfaceDeclaration) {
//            return MethodCallInfo.CallType.EXTERNAL;
//        }
//       if(owner instanceof ReflectionClassDeclaration)
//       { return MethodCallInfo.CallType.LIBRARY;}
//       if(owner instanceof JavassistClassDeclaration){
//           return MethodCallInfo.CallType.LIBRARY;
//       }
//       return MethodCallInfo.CallType.LIBRARY;
//
//
//
//    }
//
//
//}
