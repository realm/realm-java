package io.realm.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import io.realm.transformer.RealmTransformer
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.plugins.PluginCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("realm-logger")

const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"

// TODO Run a Task or Visitor to collect runtimeClassPath, then serialize it
//      Run another task that depends on the output of the first task in order to deserialize the ClassPool and process each class apart
open class Realm : Plugin<Project> {
    override fun apply(project: Project) {
        // Make sure the project is either an Android application or library
        val isAndroidApp: PluginCollection<AppPlugin> =
            project.plugins.withType(AppPlugin::class.java)
        val isAndroidLib: PluginCollection<LibraryPlugin> =
            project.plugins.withType(LibraryPlugin::class.java)

        if (isAndroidApp.isEmpty() && isAndroidLib.isEmpty()) {
            throw GradleException("'com.android.application' or 'com.android.library' plugin required.")
        }

        checkCompatibleAGPVersion()

        val isKotlinProject: Boolean =
            project.plugins.findPlugin("kotlin-android") != null || project.plugins.findPlugin("kotlin-multiplatform") != null
        val hasAnnotationProcessorConfiguration =
            project.configurations.findByName("annotationProcessor") != null
        // TODO add a parameter in 'realm' block if this should be specified by users
        val dependencyConfigurationName: String = getDependencyConfigurationName(project)
        val extension = project.extensions.create("realm", RealmPluginExtension::class.java)

        extension.isKotlinExtensionsEnabled = isKotlinProject


        RealmTransformer.register(project)

        project.dependencies.add(
            dependencyConfigurationName,
            "io.realm:realm-annotations:${Version.VERSION}"
        )
        if (isKotlinProject) {
            project.dependencies.add(
                "kapt",
                "io.realm:realm-annotations-processor:${Version.VERSION}"
            )
            project.dependencies.add(
                "kaptAndroidTest",
                "io.realm:realm-annotations-processor:${Version.VERSION}"
            )
        } else {
            assert(hasAnnotationProcessorConfiguration)
            project.dependencies.add(
                "annotationProcessor",
                "io.realm:realm-annotations-processor:${Version.VERSION}"
            )
            project.dependencies.add(
                "androidTestAnnotationProcessor",
                "io.realm:realm-annotations-processor:${Version.VERSION}"
            )
        }

        project.afterEvaluate {
            listOf(
                dependencyConfigurationName,
                ANDROID_TEST_IMPLEMENTATION // adds the dependencies to the instrumented tests configuration
            ).forEach { dependencyConfigurationName: String ->
                setDependencies(
                    project,
                    dependencyConfigurationName,
                    extension.isSyncEnabled,
                    extension.isKotlinExtensionsEnabled
                )
            }
        }

    companion object {

        private fun checkCompatibleAGPVersion() {
            val version = SimpleAGPVersion.ANDROID_GRADLE_PLUGIN_VERSION
            return when {
                version >= SimpleAGPVersion(7, 4) -> {
                    // minimum version compatible with https://developer.android.com/studio/releases/gradle-plugin-api-updates#support_for_transformations_based_on_whole_program_analysis
                    logger.debug("Realm Plugin used with AGP version: ${version.major}.${version.minor}.")
                }
                version >= SimpleAGPVersion(4, 2) -> {
                    throw GradleException("Realm Plugin used with incompatible AGP version: ${version.major}.${version.minor}. You should consider using a Realm-Java v10.11.0 or lower. Or migrate your project to use AGP 7.4 or newer.")
                }
                else -> {
                    throw GradleException("Android Gradle Plugin $version is not supported")
                }
            }
        }

        private fun getDependencyConfigurationName(project: Project): String {
            /*
             * Dependency configuration name for android gradle plugin 3.0.0-*.
             * We need to use 'api' instead of 'implementation' since user's model class
             * might be using Realm's classes and annotations.
             */
            val newDependencyName = "api"
            val oldDependencyName = "compile"
            return try {
                project.configurations.getByName(newDependencyName)
                newDependencyName
            } catch (ignored: UnknownConfigurationException) {
                oldDependencyName
            }
        }

        // This will setup the required dependencies.
        // This must only be called once, as removal of dependencies through the iterator API will
        // not propagate correctly into the IterationOrderRetainingSetElementSource which is backing
        // the dependency set deep inside the DefaultDependencySet implementation, and failure to do
        // so causes some internal caching in IterationOrderRetainingSetElementSource to skip
        // re-adding it if it had already been there once.
        private fun setDependencies(
            project: Project,
            dependencyConfigurationName: String,
            syncEnabled: Boolean,
            kotlinExtensionsEnabled: Boolean
        ) {
            // remove libraries first
            val iterator =
                project.configurations.getByName(dependencyConfigurationName).dependencies.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.group == "io.realm") {
                    if (item.name.startsWith("realm-android-library")) {
                        iterator.remove()
                    }
                    if (item.name.startsWith("realm-android-kotlin-extensions")) {
                        iterator.remove()
                    }
                }
            }

            // then add again
            val syncArtifactName =
                "realm-android-library${if (syncEnabled) "-object-server" else ""}"
            project.dependencies.add(
                dependencyConfigurationName,
                "io.realm:${syncArtifactName}:${Version.VERSION}"
            )

            if (kotlinExtensionsEnabled) {
                val kotlinExtArtifactName =
                    "realm-android-kotlin-extensions${if (syncEnabled) "-object-server" else ""}"
                project.dependencies.add(
                    dependencyConfigurationName,
                    "io.realm:${kotlinExtArtifactName}:${Version.VERSION}"
                )
            }
        }

    }
}
