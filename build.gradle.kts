allprojects {
    group = "org.worldcubeassociation.tnoodle"
    version = "0.18.1"
}

plugins {
    alias(libs.plugins.dependency.versions)
    alias(libs.plugins.nexus.publish)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

tasks.create("generateDebugRelease") {
    dependsOn(":scrambles:shadowJar")
}

allprojects {
    tasks.withType<JavaExec> {
        val existingPath = systemProperties["java.library.path"]
        systemProperties["java.library.path"] = "$existingPath:${project.rootDir}".trimStart(':')
    }
}
