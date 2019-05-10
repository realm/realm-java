/*
 * Copyright 2015 Realm Inc.
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
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Locale

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.tools.JavaFileObject

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
        writer.indent = "    "

        writer.emitPackage(Constants.REALM_PACKAGE_NAME)
        writer.emitEmptyLine()

        val attributes = LinkedHashMap<String, Boolean>()
        attributes["allClasses"] = java.lang.Boolean.TRUE
        writer.emitAnnotation(RealmModule::class.java, attributes)
        writer.beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class", // the type of the item
                emptySet(), // modifiers to apply
                null)                              // class to extend
        writer.emitEmptyLine()

        writer.endType()
        writer.close()
    }
}