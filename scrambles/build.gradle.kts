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
    implementation(project(":puzzles"))

    testImplementation(project(":state"))
    testImplementation(project(":drawing"))
    testImplementation(project(":min2phase"))
}

configureJUnit5()
