// Java源码动态编译
package com.example.demo.service;

import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.File;
import java.util.Arrays;

/**
 * DynamicCompilerService
 * ------------------------------------
 * 功能：
 * 1. 接收生成的 .java 文件
 * 2. 调用 JDK 自带的 JavaCompiler 进行动态编译
 * 3. 将 .class 输出到 generated-tests/target/classes/
 * 输入：Java源文件
 * 输出：class文件
 * 小白理解：
 * 你把 Java 文件生成了，但要执行它必须先编译成 class。
 * 这个类负责“自动编译 Java 文件”。
 */
@Service
public class DynamicCompilerService {

    private final String classOutputDir = "target/test-classes/";

    public boolean compile(File javaFile) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            System.err.println("没有找到系统 Java 编译器，请使用 JDK 而不是 JRE 运行项目");
            return false;
        }

        // 准备编译器
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        try {
            Iterable<? extends JavaFileObject> units =
                    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile));

            // 指定输出目录：-d 参数
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    null,
                    Arrays.asList("-d", classOutputDir),
                    null,
                    units
            );

            boolean success = task.call();
            fileManager.close();
            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
