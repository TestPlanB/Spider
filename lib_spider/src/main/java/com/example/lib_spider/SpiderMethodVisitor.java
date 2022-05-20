package com.example.lib_spider;

import static org.objectweb.asm.Opcodes.ASM6;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class SpiderMethodVisitor extends MethodVisitor {
    private Hook hook;
    private final List<HookMethod> methodList;


    public SpiderMethodVisitor(Hook blackHook, List<HookMethod> methodList, MethodVisitor methodVisitor, int access, String name, String descriptor, String className, String superClassName) {
        super(ASM6, methodVisitor);
        this.hook = blackHook;
        this.methodList = methodList;
    }


    @Override
    public void visitCode() {
        for (HookMethod hookMethod : methodList) {
            if (hookMethod.getHookMode().equalsIgnoreCase(HookMode.BeforeMethod)) {
                hookMethod.getCreateBytecode().call(mv);
                if (hook.isNeedLog) {
                    System.out.println("Spider hook visit code called");
                }
            }
        }

        super.visitCode();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String methodName, String descriptor, boolean isInterface) {
        if (hook.isNeedLog) {
            System.out.println("Spider====>:" + "owner:" + owner + ",methodName:" + methodName + ",descriptor:" + descriptor);
        }

        // 用倒序，只调用最新的方法，因为就算有多个方法，也是被覆盖
        for (int i = methodList.size() - 1; i >= 0; i--) {
            HookMethod hookMethod = methodList.get(i);
            if (hookMethod.getHookMode().equalsIgnoreCase(HookMode.Default)) {
                hookMethod.getCreateBytecode().call(mv);
                return;
            }
        }

        super.visitMethodInsn(opcode, owner, methodName, descriptor, isInterface);


    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ATHROW || (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
            for (HookMethod hookMethod : methodList) {
                if (hookMethod.getHookMode().equalsIgnoreCase(HookMode.AfterMethod)) {
                    hookMethod.getCreateBytecode().call(mv);
                }
            }
        }

        super.visitInsn(opcode);
    }
}
