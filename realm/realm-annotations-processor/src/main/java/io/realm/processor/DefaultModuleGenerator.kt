/*
 * Copyright 2019 Realm Inc.
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

package io.realm.processor

import com.squareup.javawriter.JavaWriter

import java.io.BufferedWriter
import java.io.IOException
import java.util.LinkedHashMap
import java.util.Locale

import javax.annotation.processing.ProcessingEnvironment

import io.realm.annotations.RealmModule

/**
 * This class is responsible for creating the DefaultRealmModule that contains all known
 * [io.realm.annotations.RealmClass]' known at compile time.
 */
class DefaultModuleGenerator(private val env: ProcessingEnvironment) {

    @Throws(IOException::class)
    fun generate() {
        val qualifiedGeneratedClassName = String.format(Locale.US, "%s.%s", Constants.REALM_PACKAGE_NAME, Constants.DEFAULT_MODULE_CLASS_NAME)
        val sourceFile = env.filer.createSourceFile(qualifiedGeneratedClassName)
        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()))

        /**
         * Defines the [io.realm.annotations.RealmModule.allClasses] attribute
         */
        val attributes = LinkedHashMap<String, Boolean>()
        attributes["allClasses"] = java.lang.Boolean.TRUE

        // Build minimal class with the required `@RealmModule` annotation for including all
        // known Realm model classes in this compilation unit.
        writer.apply {
            indent = Constants.INDENT
            emitPackage(Constants.REALM_PACKAGE_NAME)
            emitEmptyLine()
            emitAnnotation(RealmModule::class.java, attributes)
            beginType(
                    qualifiedGeneratedClassName, // full qualified name of the item to generate
                    "class",               // the type of the item
                    emptySet(),                 // modifiers to apply
                    null)           // class to extend
            emitEmptyLine()
            endType()
            close()
        }

    }
}
