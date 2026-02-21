plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.freefair.lombok") version "8.6"
}


subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.isIncremental = false // 禁用增量编译
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}