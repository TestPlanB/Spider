package com.example.lib_spider;


import groovy.lang.Closure;

public class HookMethod {
    private String owner;



    private String methodName;
    private String descriptor;
    private Closure<Void> createBytecode;
    private String hookMode;

    public String getHookMode() {
        return hookMode;
    }

    public void setHookMode(String hookMode) {
        this.hookMode = hookMode;
    }

    public HookMethod() {
    }
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public HookMethod(String mode,String owner,String methodName, String descriptor, Closure<Void> createBytecode) {
        this.owner = owner;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.createBytecode = createBytecode;
        this.hookMode = mode;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public Closure<Void> getCreateBytecode() {
        return createBytecode;
    }

    public void setCreateBytecode(Closure<Void> createBytecode) {
        this.createBytecode = createBytecode;
    }
}
