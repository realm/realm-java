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
import io.realm.transformer.ext.getBootClasspath
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
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
    val isOffline: Boolean,
    val bootClassPath: List<File>)

/**
 * This class implements the Transform API provided by the Android Gradle plugin.
 */
class RealmTransformer(project: Project,
                       private val inputs: ListProperty<Directory>,
                       private val allJars: ListProperty<RegularFile>,
                       private val referencedInputs: ConfigurableFileCollection,
                       private val output: RegularFileProperty) {
    private lateinit var metadata: ProjectMetaData
    private var analytics: RealmAnalytics? = null

    init {
        // Fetch project metadata when registering the transformer, but as some of the properties
        // we need to read might not be initialized yet (e.g. the Android extension), we need
        // to wait until after the build files have been evaluated.
        metadata = ProjectMetaData(
            // Plugin requirements
            project.gradle.startParameter.isOffline,
            project.getBootClasspath()
        )

        try {
            this.analytics = RealmAnalytics()
            this.analytics!!.calculateAnalyticsData(project)

        } catch (e: Exception) {
            // Analytics should never crash the build.
            logger.debug("Could not calculate Realm analytics data:\n$e")
        }
        transform()
    }

    companion object {

        fun register(project: Project) {
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->

                val taskProvider =
                    project.tasks.register<ModifyClassesTask>("${variant.name}RealmAccessorsTransformer", ModifyClassesTask::class.java) {
                        it.fullRuntimeClasspath.setFrom(variant.runtimeConfiguration.incoming.artifactView { c->
                            c.attributes.attribute(
                                AndroidArtifacts.ARTIFACT_TYPE,
                                AndroidArtifacts.ArtifactType.CLASSES_JAR.type
                            )
                        }.files)
                    }
                variant.artifacts.forScope(com.android.build.api.variant.ScopedArtifacts.Scope.PROJECT)
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

    /**
     * Implements the transform algorithm. The heaviest part of the transform is loading the
     * {@code CtClass} from JavaAssist, so this should be avoided as much as possible.
     *
     * This is also the reason that there are significant changes between a full build and a
     * incremental build. In a full build, we can use text matching to go from a proxy class
     * to the model class. Something we cannot do when building incrementally. In that case
     * we have to deduce all information from the class at hand.
     */
    private fun transform() {
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

    @get:OutputFiles
    abstract val output: RegularFileProperty

    @TaskAction
    fun taskAction() {
        RealmTransformer(project, allDirectories, allJars, fullRuntimeClasspath, output)
    }
}

