/*
 * Copyright 2018 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.transformer

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.UnitTest
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import io.realm.analytics.RealmAnalytics
import io.realm.transformer.build.BuildTemplate
import io.realm.transformer.build.FullBuild
import io.realm.transformer.build.IncrementalBuild
import io.realm.transformer.ext.getTargetSdk
import io.realm.transformer.ext.getMinSdk
import io.realm.transformer.ext.getBootClasspath
import io.realm.transformer.ext.getAppId
import io.realm.transformer.ext.getAgpVersion
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.execution.history.changes.IncrementalInputChanges
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.util.jar.JarOutputStream

// Package level logger
val logger: Logger = LoggerFactory.getLogger("realm-logger")

const val CONNECT_TIMEOUT = 4000L;
const val READ_TIMEOUT = 2000L;

// Wrapper for storing data from org.gradle.api.Project as we cannot store a class variable to it
// as that conflict with the Configuration Cache.
data class ProjectMetaData(
    val bootClassPath: Set<File>,
    val usesKotlin: Boolean,
    val targetType: String,
    val targetSdk: String,
    val minSdk: String,
    val agpVersion: String,
    val appId: String,
    val gradleVersion: String,
    val usesSync: Boolean,
    val isGradleOffline: Boolean
)

@Suppress("UnstableApiUsage")
fun registerRealmTransformerTask(project: Project) {
    val androidComponents =
        project.extensions.getByType(AndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
        variant.components
            .filterNot {
                // FIXME With the new transformer API changes, processed classes from the Realm transformer aren't
                // resolved correctly by the Unit tests gradle task causing the test runner not to execute tests.

                // This line disables the transformer on Unit test tasks. It is safe because Unit tests run
                // on JVM and Realm Java is not compatible with JVM.
                it is UnitTest
            }
            .forEach { component ->
                val taskProvider =
                    project.tasks.register(
                        "${component.name}RealmAccessorsTransformer",
                        RealmTransformerTask::class.java
                    ) {
                        it.referencedInputs.setFrom(component.runtimeConfiguration.incoming.artifactView { c ->
                            c.attributes.attribute(
                                AndroidArtifacts.ARTIFACT_TYPE,
                                AndroidArtifacts.ArtifactType.CLASSES_JAR.type
                            )
                        }.files)
                        it.bootClasspath.setFrom(project.getBootClasspath())
                        it.offline.set(project.gradle.startParameter.isOffline)
                        it.targetType.set(project.targetType())
                        it.usesKotlin.set(project.usesKotlin())
                        it.minSdk.set(project.getMinSdk())
                        it.targetSdk.set(project.getTargetSdk())
                        it.agpVersion.set(project.getAgpVersion())
                        it.usesSync.set(Utils.isSyncEnabled(project))
                        it.gradleVersion.set(project.gradle.gradleVersion)
                        it.appId.set(project.getAppId())
                    }
                component.artifacts.forScope(com.android.build.api.variant.ScopedArtifacts.Scope.PROJECT)
                    .use<RealmTransformerTask>(taskProvider)
                    .toTransform(
                        com.android.build.api.artifact.ScopedArtifact.CLASSES,
                        RealmTransformerTask::inputJars,
                        RealmTransformerTask::inputDirectories,
                        RealmTransformerTask::output
                    )
            }
    }
}

private fun Project.targetType(): String = with(project.plugins) {
    when {
        findPlugin("com.android.application") != null -> "app"
        findPlugin("com.android.library") != null -> "library"
        else -> "unknown"
    }
}

private fun Project.usesKotlin(): Boolean {
    return project.pluginManager.hasPlugin("kotlin-kapt")
}

/**
 * This class implements the Transform API provided by the Android Gradle plugin.
 */
abstract class RealmTransformerTask : DefaultTask() {

    @get:Classpath
    abstract val referencedInputs: ConfigurableFileCollection

    @get:InputFiles
    abstract val inputJars: ListProperty<RegularFile>

    @get:Incremental
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    @get:InputFiles
    abstract val inputDirectories: ListProperty<Directory>

    @get:InputFiles
    abstract val bootClasspath: ConfigurableFileCollection

    @get:Input
    abstract val targetType: Property<String>

    @get:Input
    abstract val offline: Property<Boolean>

    @get:Input
    abstract val usesKotlin: Property<Boolean>

    @get:Input
    abstract val targetSdk: Property<String>

    @get:Input
    abstract val minSdk: Property<String>

    @get:Input
    abstract val agpVersion: Property<String>

    @get:Input
    abstract val appId: Property<String>

    @get:Input
    abstract val gradleVersion: Property<String>

    @get:Input
    abstract val usesSync: Property<Boolean>

    @get:OutputFiles
    abstract val output: RegularFileProperty


    // Checks if a JarFile exists, if not it creates an empty one.
    private fun touchJarFile(jarFile: RegularFile) {
        if (!jarFile.asFile.exists()) {
            JarOutputStream(
                BufferedOutputStream(
                    FileOutputStream(
                        output.get().asFile
                    )
                )
            ).close()
        }
    }

    /**
     * Implements the transform algorithm. The heaviest part of the transform is loading the
     * {@code CtClass} from JavaAssist, so this should be avoided as much as possible.
     *
     * This is also the reason that there are significant changes between a full build and a
     * incremental build. In a full build, we can use text matching to go from a proxy class
     * to the model class. Something we cannot do when building incrementally. In that case
     * we have to deduce all information from the class at hand.
     */
    @TaskAction
    fun transform(inputChanges: InputChanges) {
        val metadata = ProjectMetaData(
            bootClassPath = bootClasspath.files,
            usesKotlin = usesKotlin.get(),
            targetType = targetType.get(),
            targetSdk = targetSdk.get(),
            minSdk = minSdk.get(),
            agpVersion = agpVersion.get(),
            appId = appId.get(),
            gradleVersion = gradleVersion.get(),
            usesSync = usesSync.get(),
            isGradleOffline = offline.get(),
        )

        val analytics: RealmAnalytics? = try {
            val analytics = RealmAnalytics()
            analytics.calculateAnalyticsData(metadata)
            analytics
        } catch (e: Exception) {
            // Analytics should never crash the build.
            logger.debug("Could not calculate Realm analytics data:\n$e")
            null
        }

        val timer = Stopwatch()
        val exitTransform = {
            timer.stop()
            analytics?.execute()
        }

        timer.start("Realm Transform time")

        // The output of this transform is a Jar file
        // We use FileSystem instead of a JarOutputStream because it allows modifying the entries of
        // the Jar file, and thus incremental updates.
        val jarFileOutput: FileSystem = output.get().let { jarFile ->
            // Workaround to create the Jar if does not exist, as FileSystems fails to do so.
            touchJarFile(jarFile)
            FileSystems.newFileSystem(output.get().asFile.toPath(), null)
        }

        val build: BuildTemplate =
            when {
                inputChanges.isIncremental -> IncrementalBuild(
                    metadata = metadata,
                    inputJars = inputJars.get(),
                    inputDirectories = inputDirectories.get(),
                    output = jarFileOutput,
                    incrementalInputChanges = inputChanges as IncrementalInputChanges,
                )

                else -> FullBuild(
                    metadata = metadata,
                    inputJars = inputJars.get(),
                    inputDirectories = inputDirectories.get(),
                    output = jarFileOutput,
                )
            }

        build.prepareOutputClasses()
        timer.splitTime("Prepare output classes")
        if (build.hasNoOutput()) {
            // Abort transform as quickly as possible if no files where found for processing.
            exitTransform()
            return
        }
        build.prepareReferencedClasses(referencedInputs)
        timer.splitTime("Prepare referenced classes")
        build.markMediatorsAsTransformed()
        timer.splitTime("Mark mediators as transformed")
        build.transformModelClasses()
        timer.splitTime("Transform model classes")
        build.transformDirectAccessToModelFields()
        timer.splitTime("Transform references to model fields")
        build.copyProcessedClasses()
        timer.splitTime("Copy processed classes")
        build.copyResourceFiles()
        timer.splitTime("Copy jar files")
        jarFileOutput.close()
        exitTransform()
    }
}

