plugins {
    `java-library`
}

dependencies {
    implementation(project(":API"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper:1.21.1-R0.1-SNAPSHOT")
    compileOnly("it.unimi.dsi:fastutil:8.5.9")
}