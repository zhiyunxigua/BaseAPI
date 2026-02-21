import org.gradle.kotlin.dsl.invoke

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
    id("io.freefair.lombok")
}

dependencies {
    // 依赖API模块
    api(project(":API"))
    api(project(":cumulus"))

    // 构建时依赖
    compileOnly(libs.spigot.api)

    compileOnly(libs.fastutil)

    // Netty (for ByteBuf)
    compileOnly(libs.netty.buffer)

    // 运行时依赖
    api("com.google.inject", "guice", "6.0.0")
    // http服务
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.5")

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

        //from(project(":v1_18_R2").sourceSets.main.get().output)
        //from(project(":v1_21").sourceSets.main.get().output)

        minimize ()
    }

    build {
        dependsOn(shadowJar)
    }
}