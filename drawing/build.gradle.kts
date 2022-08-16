import configurations.Languages.attachRemoteRepositories
import configurations.Languages.configureJava
import configurations.Frameworks.configureCheckstyle
import configurations.Publications.configureMavenPublication
import configurations.Publications.configureSignatures

description = "A dead simple SVG generation library written in pure Java, with no dependencies. This code runs on both desktop Java, Android, and compiles to Javascript with GWT."

attachRemoteRepositories()

plugins {
    `java-library`
    checkstyle
    `maven-publish`
    signing
}

configureJava()
configureCheckstyle()
configureMavenPublication("lib-drawing")
configureSignatures(publishing)

dependencies {
    implementation(project(":state"))
}

//configureJUnit5()
