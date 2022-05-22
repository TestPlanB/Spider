# Spider
Spider是基于gradle配置的动态插桩实现，只需简单配置即可完成ASM插桩工作，可用于隐私号整改，代码扫描等多个用途，欢迎pr！

## Spider目标定位在通过简单的gradle配置，即可实现方法插桩，无需了解底层transform的编写以及ASM的语法，即可定义自己的hook规则
与市面上常见的Hook相比，如Aspect J ，有如下特点：
* **定位准确，简单的gradle配置即可hook任意方法**
* **可自定义hook规则，无论是ASM新手还是老手，都可以通过编写method visitor的行为，去执行hook**
* **轻量级，可直接实现源码级别接入，只需自定义本地仓库即可**
* **基于ASM，效率Up up up**
* **流程通俗易懂，便于学习和二次开发**

## 原理解析
(https://juejin.cn/post/7100086790639337508)

## 使用说明
### 本地使用
```
该项目可以用于本地配置使用，只需拷贝lib_spider这个module到自己的项目即可，请按照以下条件使用（通用的module使用方法）
1.拷贝lib_spider这个module到自己的项目
2.项目的settings.gralde 添加上include ':lib_spider'，如项目工程所示
3.gradle执行task uploadArchives 即可，Androidstudio可以右上角点击大象图标直接输入执行
4.根目录的build.gralde 添加依赖 classpath 'com.spider.plugins:classvisitor-plugin:1.1.3' 如项目所示，可以自定义版本号 sync
5.app目录的builde.gralde 或者其他gradle文件依赖apply plugin: 'com.spider.plugins' 如项目的app中的build.gradle所示 sync
6.编写自定义规则

```

### 自定义规则编写
例子如下：hook里面可以编写规则，无特别需求可以如下所示，我们要关注hookMethods里面，一个hookMethods有多个hookmethod 方法可以使用。
* **第一个参数就是定义好的模式，Spider提供了以下模式：BeforeMethod（方法前） Default（方法本身） AfterMethod（方法执行后）以下模式：BeforeMethod（方法前） Default（方法本身） AfterMethod（方法执行后）**
* **第二个参数是我们需要hook的类名**
* **第三个参数是需要hook的方法**
* **第四个是方法签名**
* **第五个是一个groovy写法的闭包，可以理解为函数，里面有一个MethodVisitor的参数，我们可以利用这个做到自定义修改规则，不熟悉ASM的朋友可以利用ASM bytecode viewer插件（可以在AS里面下载），生成我们想要的字节码规则。**

```
比如我想在MainActivity 的 onCreate方法开始前插入一个方法，
hookMethod hookMode.BeforeMethod, "com/example/spider/MainActivity", "onCreate", "(Landroid/os/Bundle;)V", { MethodVisitor mv ->
            println("before")
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/spider/LogTest", "before", "()V", false)
        }
----------------------------------
又比如我想替换LogUtils类里面的test方法为LogTest类里面的静态方法test
 hookMethod hookMode.Default,  "com/example/spider/LogUtils", "test", "()V", { MethodVisitor mv ->

            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/spider/LogTest", "test", "()V", false)

        }

-----------------------------------
项目例子：

hook {
    //表示要处理的数据类型是什么，CLASSES 表示要处理编译后的字节码(可能是 jar 包也可能是目录)，RESOURCES 表示要处理的是标准的 java 资源
    inputTypes CONTENT_CLASS
    //表示Transform 的作用域，这里设置的SCOPE_FULL_PROJECT代表作用域是全工程
    scopes SCOPE_FULL_PROJECT
    //表示是否支持增量编译，false不支持
    isIncremental false
    //是否打印扫描到的方法的信息，包含类名，方法名，方法签名
    isNeedLog true
    //表示需要被hook的方法
    hookMethods {
        println("hookMethodList ")
        // 一下参数意思：需要hook的模式 / 被hook类 /类中的方法 / 方法签名
        hookMethod hookMode.BeforeMethod, "com/example/spider/MainActivity", "onCreate", "(Landroid/os/Bundle;)V", { MethodVisitor mv ->
            println("before")
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/spider/LogTest", "before", "()V", false)
        }

        hookMethod hookMode.Default,  "com/example/spider/LogUtils", "test", "()V", { MethodVisitor mv ->

            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/spider/LogTest", "test", "()V", false)

        }

        hookMethod hookMode.AfterMethod, "com/example/spider/MainActivity", "onCreate", "(Landroid/os/Bundle;)V", { MethodVisitor mv ->
            println("after")
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/spider/LogTest", "after", "()V", false)
        }
    }
}
```
## 项目层级介绍
* **app下是使用例子**
* **lib_spider 是Spider的封装实现**

## 环境准备
建议直接用最新的稳定版本Android Studio打开工程。目前项目已适配`Android Studio Arctic Fox | 2020.3.1`，目前支持gradle6及以下版本
低版本的Android Studio可能因为Gradle版本过高而无法正常打开项目。
### 
