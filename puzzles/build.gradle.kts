import configurations.Languages.attachRemoteRepositories
import configurations.Languages.configureJava
import configurations.Frameworks.configureCheckstyle
import configurations.Publications.configureMavenPublication
import configurations.Publications.configureSignatures

description = "Puzzle definitions for Java scrambles"

attachRemoteRepositories()

plugins {
    `java-library`
    checkstyle
    `maven-publish`
    signing
}

configureJava()
configureCheckstyle()
configureMavenPublication("data-puzzles")
configureSignatures(publishing)

dependencies {
    implementation(project(":drawing"))
    implementation(project(":state"))

    api(libs.gwt.exporter)
}
