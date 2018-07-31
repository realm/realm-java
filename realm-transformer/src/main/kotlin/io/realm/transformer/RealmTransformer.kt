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
import io.realm.transformer.build.FullBuild
import io.realm.transformer.build.IncrementalBuild
import io.realm.transformer.build.BuildTemplate
import io.realm.transformer.ext.getMinSdk
import io.realm.transformer.ext.getTargetSdk
import javassist.CtClass
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

// Package level logger
val logger: Logger = LoggerFactory.getLogger("realm-logger")

/**
 * This class implements the Transform API provided by the Android Gradle plugin.
 */
class RealmTransformer(val project: Project) : Transform() {

    val logger: Logger = LoggerFactory.getLogger("realm-logger")

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

        val build: BuildTemplate = if (isIncremental) IncrementalBuild(project, outputProvider!!, this)
        else FullBuild(project, outputProvider!!, this)

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
        this.sendAnalytics(inputs, outputModelClasses)
    }

    /**
     * Sends the analytics
     *
     * @param inputs the inputs provided by the Transform API
     * @param inputModelClasses a list of ctClasses describing the Realm models
     */
    private fun sendAnalytics(inputs: Collection<TransformInput>, outputModelClasses: Set<CtClass>) {
        try {
            val disableAnalytics: Boolean = "true".equals(System.getenv()["REALM_DISABLE_ANALYTICS"], ignoreCase = true)
            if (inputs.isEmpty() || disableAnalytics) {
                // Don't send analytics for incremental builds or if they have been explicitly disabled.
                return
            }

            var containsKotlin = false

            outer@
            for(input: TransformInput in inputs) {
                for (di: DirectoryInput in input.directoryInputs) {
                    val path: String = di.file.absolutePath
                    val index: Int = path.indexOf("build${File.separator}intermediates${File.separator}classes")
                    if (index != -1) {
                        val projectPath: String = path.substring(0, index)
                        val buildFile = File(projectPath + "build.gradle")
                        if (buildFile.exists() && buildFile.readText().contains("kotlin")) {
                            containsKotlin = true
                            break@outer
                        }
                    }
                }
            }

            val packages: Set<String> = outputModelClasses.map {
                it.packageName
            }.toSet()

            val targetSdk: String? = project.getTargetSdk()
            val minSdk: String?  = project.getMinSdk()

            if (!disableAnalytics) {
                val sync: Boolean = Utils.isSyncEnabled(project)
                val analytics = RealmAnalytics(packages, containsKotlin, sync, targetSdk, minSdk)
                analytics.execute()
            }
        } catch (e: Exception) {
            // Analytics failing for any reason should not crash the build
            logger.debug("Could not send analytics: $e")
        }
    }

}
