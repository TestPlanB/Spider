package com.example.lib_spider;

import static org.objectweb.asm.Opcodes.ASM6;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import java.util.ArrayList;
import java.util.List;

public class SpiderClassVisitor extends ClassVisitor {
    private String className;
    private Hook hook;
    private String superClassName;
    private HookMethod method;
    boolean need = false;

    public SpiderClassVisitor(ClassVisitor classVisitor, Hook hook) {
        super(ASM6, classVisitor);
        this.hook = hook;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
        superClassName = superName;
        if (hook != null && hook.hookMethodList != null && hook.hookMethodList.size() > 0) {
            for (int i = 0; i < hook.hookMethodList.size(); i++) {
                HookMethod hookMethod = hook.hookMethodList.get(i);
                if (name.equals(hookMethod.getOwner().trim())) {
                    need = true;
                    return;
                }
            }
        }

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

        if (!need) {
            return mv;
        }
        if (hook.isNeedLog) {
            System.out.println("Spider hook method and class " + name + " " + className);
        }
        if (hook != null && hook.hookMethodList != null && hook.hookMethodList.size() > 0) {
            List<HookMethod> hookMethodList = new ArrayList<>();
            for (int i = 0; i < hook.hookMethodList.size(); i++) {
                HookMethod hookMethod = hook.hookMethodList.get(i);
                if (name.equalsIgnoreCase(hookMethod.getMethodName().trim()) && descriptor.equalsIgnoreCase(hookMethod.getDescriptor())) {
                    hookMethodList.add(hookMethod);
                }
            }

            return new SpiderMethodVisitor(hook, hookMethodList, mv, access, name, descriptor, className, superClassName);
        }
        return mv;
    }


}
