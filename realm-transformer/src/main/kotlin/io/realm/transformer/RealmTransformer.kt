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
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import io.realm.analytics.RealmAnalytics
import io.realm.transformer.build.BuildTemplate
import io.realm.transformer.build.FullBuild
import io.realm.transformer.ext.getTargetSdk
import io.realm.transformer.ext.getMinSdk
import io.realm.transformer.ext.getBootClasspath
import io.realm.transformer.ext.getAppId
import io.realm.transformer.ext.getAgpVersion
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
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
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarOutputStream

// Package level logger
val logger: Logger = LoggerFactory.getLogger("realm-logger")

val CONNECT_TIMEOUT = 4000L;
val READ_TIMEOUT = 2000L;

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
        val isGradleOffline: Boolean) {
}

/**
 * This class implements the Transform API provided by the Android Gradle plugin.
 */
class RealmTransformer(private val metadata: ProjectMetaData,
                       private val inputs: ListProperty<Directory>,
                       private val allJars: ListProperty<RegularFile>,
                       private val referencedInputs: ConfigurableFileCollection,
                       private val output: RegularFileProperty) {

    private val analytics: RealmAnalytics? = try {
        val analytics = RealmAnalytics()
        analytics.calculateAnalyticsData(metadata)
        analytics
    } catch (e: Exception) {
        // Analytics should never crash the build.
        logger.debug("Could not calculate Realm analytics data:\n$e")
        null
    }

    companion object {

        fun register(project: Project) {
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                variant.components.forEach { component ->
                    val taskProvider =
                        project.tasks.register(
                            "${component.name}RealmAccessorsTransformer",
                            ModifyClassesTask::class.java
                        ) {
                            it.fullRuntimeClasspath.setFrom(component.runtimeConfiguration.incoming.artifactView { c ->
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
                        .use<ModifyClassesTask>(taskProvider)
                        .toTransform(
                            com.android.build.api.artifact.ScopedArtifact.CLASSES,
                            ModifyClassesTask::allJars,
                            ModifyClassesTask::allDirectories,
                            ModifyClassesTask::output
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
            for (conf in project.configurations) {
                try {
                    for (artifact: ResolvedArtifact in conf.resolvedConfiguration.resolvedArtifacts) {
                        if (artifact.name.startsWith("kotlin-stdlib")) {
                            return true
                        }
                    }
                } catch (ignore: Exception) {
                    // Some artifacts might not be able to resolve, in this case, just ignore them.
                }
            }
            return false
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
    internal fun transform() {
        val timer = Stopwatch()
        timer.start("Realm Transform time")

        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )

        val build: BuildTemplate = FullBuild(metadata, allJars, jarOutput, this)

        build.prepareOutputClasses(inputs)
        timer.splitTime("Prepare output classes")
        if (build.hasNoOutput()) {
            // Abort transform as quickly as possible if no files where found for processing.
            exitTransform(timer)
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
        jarOutput.close()
        exitTransform(timer)
    }

    private fun exitTransform(timer: Stopwatch) {
        timer.stop()
        analytics?.execute()
    }
}

abstract class ModifyClassesTask: DefaultTask() {

    @get:Classpath
    abstract val fullRuntimeClasspath : ConfigurableFileCollection

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

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

    @TaskAction
    fun taskAction() {
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
        RealmTransformer(
                metadata = metadata,
                inputs = allDirectories,
                allJars = allJars,
                referencedInputs = fullRuntimeClasspath,
                output = output,
        ).transform()
    }
}

