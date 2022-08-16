import configurations.Languages.attachRemoteRepositories
import configurations.Languages.configureJava
import configurations.Frameworks.configureCheckstyle
import configurations.Frameworks.configureJUnit5
import configurations.Publications.configureMavenPublication
import configurations.Publications.configureSignatures

description = "Data for ensuring we always have scrambles for all WCA events."

plugins {
    `java-library`
    checkstyle
    `maven-publish`
    signing
}

attachRemoteRepositories()

configureJava()
configureCheckstyle()
configureMavenPublication("data-wca")
configureSignatures(publishing)
