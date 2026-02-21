plugins {
    `java-library`
}

dependencies {
    implementation(project(":API"))
    compileOnly("org.spigotmc:spigot:1.18.2-R0.1-SNAPSHOT")

    compileOnly(libs.spigot.api)
    compileOnly(libs.fastutil)
}