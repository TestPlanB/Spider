package com.spider.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.example.lib_spider.Hook
import com.example.lib_spider.HookMethod
import com.example.lib_spider.HookMethodList
import com.example.lib_spider.HookMode
import com.example.lib_spider.SpiderClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.utils.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

class SpiderPlugin extends Transform implements Plugin<Project> {

    Project project

    Hook hook

    @Override
    void apply(Project target) {
        println("spider plugin start ")
        project = target
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(this)
        target.extensions.create("hook", Hook.class)
        target.extensions.create("hookMode", HookMode.class)
        target.hook.extensions.create("hookMethods", HookMethodList.class)


        project.tasks.create('printHook').doFirst {
            System.out.println("hook params is " + project.hook.hookMethodList)
        }
    }

    @Override
    String getName() {
        return "SpiderPlugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        if (Hook.CONTENT_CLASS == project.hook.inputTypes) {
            return TransformManager.CONTENT_CLASS
        } else if (Hook.CONTENT_JARS == project.hook.inputTypes) {
            return TransformManager.CONTENT_JARS
        } else if (Hook.CONTENT_RESOURCES == project.hook.inputTypes) {
            return TransformManager.CONTENT_RESOURCES
        }
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        if (Hook.SCOPE_FULL_PROJECT == project.hook.scopes) {
            return TransformManager.SCOPE_FULL_PROJECT
        } else if (Hook.PROJECT_ONLY == project.hook.scopes) {
            return TransformManager.PROJECT_ONLY
        }
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {

        return project.hook.isIncremental
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Collection<TransformInput> inputs = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }
        if (hook == null) {
            hook = new Hook()
            hook.methodHooker = project.hook.methodHooker
            hook.isNeedLog = project.hook.isNeedLog
//            HookMethodList list = new HookMethodList()
            HookMethodList list = project.hook.hookMethods
            System.out.println("list "+project.hook.hookMethods +" "+project.hook.extensions.hookMethods)
            System.out.println("list "+list.methodList.size())
            if (list.methodList.size() > 0) {
                hook.hookMethodList.addAll(list.methodList)
            }

//            for (int i = 0; i < list.methodList.size(); i++) {
//                HookMethod hookMethod = new HookMethod()
//                hookMethod.owner = project.hook.hookMethodList.get(i).owner
//                hookMethod.className = project.hook.hookMethodList.get(i).className
//                hookMethod.methodName = project.hook.hookMethodList.get(i).methodName
//                hookMethod.descriptor = project.hook.hookMethodList.get(i).descriptor
//                hookMethod.createBytecode = project.hook.hookMethodList.get(i).createBytecode
//                hookMethod.hookMode = project.hook.hookMethodList.get(i).hookMode
//                hook.hookMethodList.addAll(hookMethod)
//            }

//            blackHook.hookMethodList = project.extensions.blackHook.hookMethodList
        }
        inputs.each { input ->
            input.directoryInputs.each { directoryInput ->
                handleDirectoryInput(directoryInput, outputProvider)

            }

            //遍历jarInputs
            input.jarInputs.each { JarInput jarInput ->
                //处理jarInputs
                handleJarInputs(jarInput, outputProvider)
            }
        }
        super.transform(transformInvocation)

    }

    void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        if (directoryInput.file.isDirectory()) {
            directoryInput.file.eachFileRecurse { file ->
                String name = file.name
                if (name.endsWith(".class") && !name.startsWith("R\$drawable")
                        && !"R.class".equals(name) && !"BuildConfig.class".equals(name)) {
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor classVisitor = new SpiderClassVisitor(classWriter, hook)
                    classReader.accept(classVisitor, EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(
                            file.parentFile.absolutePath + File.separator + name)
                    fos.write(code)
                    fos.close()
                }
            }
        }

        //处理完输入文件之后，要把输出给下一个任务
        def dest = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes,
                Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    void handleJarInputs(JarInput jarInput, TransformOutputProvider outputProvider) {
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            def jarName = jarInput.name

            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
            //用于保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(jarEntry)

                if (entryName.endsWith(".class") && !entryName.startsWith("R\$")
                        && !"R.class".equals(entryName) && !"BuildConfig.class".equals(entryName)) {
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new SpiderClassVisitor(classWriter, hook)
                    classReader.accept(cv, EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }

                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
            def dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
        }
    }
}


