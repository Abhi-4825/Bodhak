//package com.example.bodhakfrontend.Overview;
//
//import com.example.bodhakfrontend.Models.ClassDependencyInfo;
//import com.example.bodhakfrontend.Models.MethodCallInfo;
//import com.example.bodhakfrontend.Models.MethodsInfo;
//
//
//import java.util.*;
//
//public class MethodDependeciesBuilder {
//    public Map<String, List<MethodCallInfo>> build(ClassDependencyInfo classDependencyInfo,String className){
//        Map<String, List<MethodCallInfo >> calledMethods = new HashMap<>();
//
//        List<MethodsInfo> methods =
//                classDependencyInfo.getMethods()
//                        .getOrDefault(className, Collections.emptyList());
//
//        for(MethodsInfo methodsInfo:methods){
//            List<MethodCallInfo> depsOn=methodsInfo.getCalledMethods();
//            if (depsOn == null || depsOn.isEmpty()) {
//                continue;
//            }
//
//        calledMethods.computeIfAbsent(methodsInfo.getMethodName(),k -> new ArrayList<>()).addAll(depsOn);
//        }
//        return calledMethods;
//    }
//
//
//}
