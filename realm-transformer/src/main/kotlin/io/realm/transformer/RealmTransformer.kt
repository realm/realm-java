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

import com.android.build.api.transform.*
import io.realm.analytics.RealmAnalytics
import io.realm.transformer.build.BuildTemplate
import io.realm.transformer.build.FullBuild
import io.realm.transformer.build.IncrementalBuild
import io.realm.transformer.ext.getBootClasspath
import javassist.CtClass
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

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
class RealmTransformer(project: Project) : Transform() {
    private val logger: Logger = LoggerFactory.getLogger("realm-logger")
    private val metadata: ProjectMetaData
    private lateinit var analytics: RealmAnalytics

    init {
        // Fetch project metadata when registering the transformer, as the Project is not
        // available during execution time when using the Configuration Cache.
        metadata = ProjectMetaData(
            // Plugin requirements
            project.gradle.startParameter.isOffline,
            project.getBootClasspath()
        )

        // We need to fetch analytics data at evaluation time as the Project class is not
        // available at execution time when using the Configuration Cache
        project.afterEvaluate {
            this.analytics = RealmAnalytics()
            try {
                this.analytics.calculateAnalyticsData(project)
            } catch (e: Exception) {
                // Analytics should never crash the build.
                logger.debug("Could not calculate Realm analytics data:\n$e" )
            }
        }
    }
    override fun getName(): String {
        return "RealmTransformer"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return setOf<QualifiedContent.ContentType>(QualifiedContent.DefaultContentType.CLASSES)
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return mutableSetOf(QualifiedContent.Scope.PROJECT)
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
        // Scope.PROJECT_LOCAL_DEPS and Scope.SUB_PROJECTS_LOCAL_DEPS is only for compatibility with AGP 1.x, 2.x
        return mutableSetOf(
                QualifiedContent.Scope.EXTERNAL_LIBRARIES,
                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.TESTED_CODE
        )
    }

    /**
     * Implements the transform algorithm. The heaviest part of the transform is loading the
     * {@code CtClass} from JavaAssist, so this should be avoided as much as possible.
     *
     * This is also the reason that there are significant changes between a full build and a
     * incremental build. In a full build, we can use text matching to go from a proxy class
     * to the model class. Something we cannot do when building incrementally. In that case
     * we have to deduce all information from the class at hand.
     *
     * @param context
     * @param inputs
     * @param referencedInputs
     * @param outputProvider
     * @param isIncremental
     * @throws IOException
     * @throws TransformException
     * @throws InterruptedException
     */
    override fun transform(context: Context?, inputs: MutableCollection<TransformInput>?,
                           referencedInputs: Collection<TransformInput>?,
                           outputProvider: TransformOutputProvider?, isIncremental: Boolean) {

        val timer = Stopwatch()
        timer.start("Realm Transform time")

        val build: BuildTemplate = if (isIncremental) IncrementalBuild(metadata, outputProvider!!, this)
        else FullBuild(metadata, outputProvider!!, this)

        build.prepareOutputClasses(inputs!!)
        timer.splitTime("Prepare output classes")
        if (build.hasNoOutput()) {
            // Abort transform as quickly as possible if no files where found for processing.
            exitTransform(emptySet(), emptySet(), timer)
            return
        }
        build.prepareReferencedClasses(referencedInputs!!)
        timer.splitTime("Prepare referenced classes")
        build.markMediatorsAsTransformed()
        timer.splitTime("Mark mediators as transformed")
        build.transformModelClasses()
        timer.splitTime("Transform model classes")
        build.transformDirectAccessToModelFields()
        timer.splitTime("Transform references to model fields")
        build.copyResourceFiles()
        timer.splitTime("Copy resource files")
        exitTransform(inputs, build.getOutputModelClasses(), timer)
    }

    private fun exitTransform(inputs: Collection<TransformInput>, outputModelClasses: Set<CtClass>, timer: Stopwatch) {
        timer.stop()
        analytics.execute()
    }
}
