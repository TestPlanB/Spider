package com.example.lib_spider;

import java.util.ArrayList;
import java.util.List;
import groovy.lang.Closure;

public class HookMethodList {
    public List<HookMethod> methodList= new ArrayList<>();

    public void hookMethod(String hookMode,String owner,String methodName,String descriptor,Closure<Void> createBytecode){
        System.out.println("spider hookMethod add ");
        methodList.add(new HookMethod(hookMode,owner,methodName,descriptor,createBytecode));
        System.out.println("spider hookMethod list "+methodList.size()+" "+methodList);
    }
}
