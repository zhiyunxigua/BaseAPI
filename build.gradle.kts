plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.freefair.lombok") version "8.6"
}

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

dependencies {
    api(project(":cumulus"))

    // 构建时依赖
    compileOnly(libs.spigot.api)

    // Netty (for ByteBuf)
    compileOnly(libs.netty.buffer)

    // 运行时依赖
    api("com.google.inject:guice:6.0.0")
    // http服务
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.5")

    implementation(libs.fastutil)

    // Nimbus JOSE + JWT
    implementation("com.nimbusds:nimbus-jose-jwt:9.25")

    // MessagePack
    implementation("org.msgpack:msgpack-core:0.9.0")

    // NukkitX Protocol (for VarInts and Zlib)
    implementation("com.nukkitx.network:raknet:1.6.28-SNAPSHOT")
}

tasks {
    shadowJar {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
        archiveClassifier.set("")
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))

        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
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
