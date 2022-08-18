import configurations.Languages.attachRemoteRepositories
import configurations.Languages.configureJava
import configurations.Frameworks.configureCheckstyle
import configurations.Frameworks.configureJUnit5
import configurations.Publications.configureMavenPublication
import configurations.Publications.configureSignatures

description = "A Java scrambling suite. Java applications can use this project as a library. A perfect example of this is the webscrambles package."

plugins {
    `java-library`
    checkstyle
    `maven-publish`
    signing
    alias(libs.plugins.shadow)
}

attachRemoteRepositories()

configureJava()
configureCheckstyle()
configureMavenPublication("lib-scrambles")
configureSignatures(publishing)

dependencies {
    api(project(":svglite"))

    implementation(project(":min2phase"))
    implementation(project(":threephase"))
    implementation(project(":sq12phase"))

    api(libs.gwt.exporter)
}

configureJUnit5()

tasks.create<JavaCompile>("generateJniHeaders") {
    classpath = configurations["runtimeClasspath"]

    val jniDir = "$buildDir/generated/jni"

    destinationDirectory.set(file(jniDir))
    options.compilerArgs.addAll(listOf("-h", jniDir))

    source = sourceSets["main"].java
}
