import dependencies.Plugins.DEPENDENCY_VERSIONS_ACTUAL
import dependencies.Plugins.GIT_VERSION_TAG_ACTUAL
import dependencies.Plugins.JFROG_BINTRAY_ACTUAL
import dependencies.Plugins.SHADOW_ACTUAL
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

// This currently only exists because there is a bug in Gradle
// that prevents "plugins {}" and "buildscript {}" blocks
// to load anything not in the root package.

// See https://github.com/gradle/gradle/issues/9270 for details.

inline val PluginDependenciesSpec.SHADOW: PluginDependencySpec
    get() = SHADOW_ACTUAL

inline val PluginDependenciesSpec.DEPENDENCY_VERSIONS: PluginDependencySpec
    get() = DEPENDENCY_VERSIONS_ACTUAL

inline val PluginDependenciesSpec.GIT_VERSION_TAG: PluginDependencySpec
    get() = GIT_VERSION_TAG_ACTUAL

inline val PluginDependenciesSpec.JFROG_BINTRAY: PluginDependencySpec
    get() = JFROG_BINTRAY_ACTUAL
