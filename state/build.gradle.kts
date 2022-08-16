import configurations.Languages.attachRemoteRepositories
import configurations.Languages.configureJava
import configurations.Frameworks.configureCheckstyle
import configurations.Publications.configureMavenPublication
import configurations.Publications.configureSignatures

description = "Internal state representations for Java scrambles"

attachRemoteRepositories()

plugins {
    `java-library`
    checkstyle
    `maven-publish`
    signing
}

configureJava()
configureCheckstyle()
configureMavenPublication("data-state")
configureSignatures(publishing)

dependencies {
    implementation(project(":min2phase"))
    implementation(project(":threephase"))
    implementation(project(":sq12phase"))
}
