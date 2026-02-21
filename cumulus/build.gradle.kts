plugins {
    `java-library`
}

dependencies {
    implementation(libs.gson)

    compileOnlyApi(libs.checker.qual)
}