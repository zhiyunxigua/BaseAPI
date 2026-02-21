plugins {
    `java-library`
    id("io.freefair.lombok")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")

    // MessagePack
    implementation("org.msgpack:msgpack-core:0.9.0")
}