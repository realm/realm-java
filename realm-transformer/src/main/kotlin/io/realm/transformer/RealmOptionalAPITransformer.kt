/*
 * Copyright 2016 Realm Inc.
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

import com.android.build.api.transform.Format
import com.android.build.api.transform.Context
import com.android.build.api.transform.Transform
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.google.common.collect.ImmutableSet
import io.realm.annotations.internal.OptionalAPI
import io.realm.transformer.util.appendThisToClassNames
import io.realm.transformer.util.appendThisToClassPool
import io.realm.transformer.util.createClassPool
import org.slf4j.LoggerFactory
import java.util.HashSet

class RealmOptionalAPITransformer : Transform() {

    private val logger = LoggerFactory.getLogger("realm-logger")
    private val transformerName = "realm-optional-api"

    override fun getName(): String? = "RealmOptionalAPITransformer"

    override fun isIncremental(): Boolean = false

    override fun getInputTypes(): MutableSet<ContentType>? = ImmutableSet.of(DefaultContentType.CLASSES)

    override fun getScopes(): MutableSet<Scope>? = ImmutableSet.of(Scope.EXTERNAL_LIBRARIES)

    override fun getReferencedScopes(): MutableSet<Scope>? = ImmutableSet.of(Scope.PROJECT, Scope.PROJECT_LOCAL_DEPS,
            Scope.SUB_PROJECTS, Scope.SUB_PROJECTS_LOCAL_DEPS, Scope.EXTERNAL_LIBRARIES)

    override fun transform(context: Context?,
                           inputs: MutableCollection<TransformInput>?,
                           referencedInputs: MutableCollection<TransformInput>?,
                           outputProvider: TransformOutputProvider?,
                           isIncremental: Boolean) {

        val classNames = HashSet<String>()
        inputs!!.appendThisToClassNames(classNames)
        val refClassNames = HashSet<String>()
        referencedInputs!!.appendThisToClassNames(refClassNames)
        val classPool = createClassPool()
        inputs.appendThisToClassPool(classPool)

        classNames.filter { it.startsWith("io.realm.") }.forEach {
            classPool.get(it).declaredMethods.forEach {
                val optionalAPIAnnotation = it.getAnnotation(OptionalAPI::class.java) as? OptionalAPI
                val dependenciesList = optionalAPIAnnotation?.dependencies?.toList()

                if (optionalAPIAnnotation == null) {
                    logger.debug("${it.declaringClass.name} ${it.name} doesn't have @OptionalAPI annotation.")
                } else if (dependenciesList == null || dependenciesList.size == 0) {
                    throw IllegalArgumentException("${it.name} doesn't have proper dependencies: " +
                            "${optionalAPIAnnotation.dependencies}.")
                } else if (!refClassNames.containsAll(dependenciesList)) {
                    // Doesn't have enough dependencies, remove the API
                    logger.debug("${it.declaringClass.name} ${it.name} will be removed since some of the dependencies " +
                            "in $dependenciesList don't exist.")
                    it.declaringClass.removeMethod(it)
                } else {
                    logger.debug("${it.declaringClass.name} ${it.name} has all dependencies in $dependenciesList.")
                }
            }
        }

        // Create outputs
        classNames.forEach {
            val ctClass = classPool.getCtClass(it)
            ctClass.writeFile(
                    outputProvider!!.getContentLocation(transformerName, inputTypes, scopes, Format.DIRECTORY).canonicalPath)
        }
    }
}
