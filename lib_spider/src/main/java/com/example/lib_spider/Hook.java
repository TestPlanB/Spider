package com.example.lib_spider;

import java.util.ArrayList;
import java.util.List;

import groovy.lang.Closure;

/**
 * 配置的hook类
 */

public class Hook {
    public static final String CONTENT_CLASS = "CONTENT_CLASS";
    public static final String CONTENT_JARS = "CONTENT_JARS";
    public static final String CONTENT_RESOURCES = "CONTENT_RESOURCES";
    public static final String SCOPE_FULL_PROJECT = "SCOPE_FULL_PROJECT";
    public static final String PROJECT_ONLY = "PROJECT_ONLY";

    public Closure methodHooker;

    public List<HookMethod> hookMethodList = new ArrayList<>();


    public String inputTypes = CONTENT_CLASS;

    public String scopes = SCOPE_FULL_PROJECT;

    boolean isNeedLog = false;

    boolean isIncremental = false;

    public Closure getMethodHooker() {
        return methodHooker;
    }

    public void setMethodHooker(Closure methodHooker) {
        this.methodHooker = methodHooker;
    }

    public List<HookMethod> getHookMethodList() {
        return hookMethodList;
    }

    public void setHookMethodList(List<HookMethod> hookMethodList) {
        this.hookMethodList = hookMethodList;
    }

    public String getInputTypes() {
        return inputTypes;
    }

    public void setInputTypes(String inputTypes) {
        this.inputTypes = inputTypes;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public boolean getIsIncremental() {
        return isIncremental;
    }

    public void setIsIncremental(boolean incremental) {
        isIncremental = incremental;
    }

    public boolean getIsNeedLog() {
        return isNeedLog;
    }

    public void setIsNeedLog(boolean needLog) {
        isNeedLog = needLog;
    }
}
