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
import java.util.EnumSet
import java.util.Locale

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

import io.realm.annotations.Ignore


class RealmProxyInterfaceGenerator(private val processingEnvironment: ProcessingEnvironment, private val metaData: ClassMetaData) {

    private val className: QualifiedClassName = metaData.qualifiedClassName

    @Throws(IOException::class)
    fun generate() {
        val qualifiedGeneratedInterfaceName = String.format(Locale.US, "%s.%s", Constants.REALM_PACKAGE_NAME, Utils.getProxyInterfaceName(className))
        val sourceFile = processingEnvironment.filer.createSourceFile(qualifiedGeneratedInterfaceName)
        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()!!))
        writer.apply {
            indent = Constants.INDENT
            emitPackage(Constants.REALM_PACKAGE_NAME)
            emitEmptyLine()
            beginType(qualifiedGeneratedInterfaceName, "interface", EnumSet.of(Modifier.PUBLIC))

            for (field in metaData.fields) {
                if (field.modifiers.contains(Modifier.STATIC) || field.getAnnotation(Ignore::class.java) != null) {
                    continue
                }
                // The field is neither static nor ignored
                val fieldName = field.simpleName.toString()
                val fieldTypeCanonicalName = field.asType().toString()
                beginMethod(fieldTypeCanonicalName, metaData.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                endMethod()

                // MutableRealmIntegers do not have setters.
                if (Utils.isMutableRealmInteger(field)) {
                    continue
                }
                beginMethod("void", metaData.getInternalSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value")
                endMethod()
            }

            // backlinks are final and have only a getter.
            for (backlink in metaData.backlinkFields) {
                beginMethod(backlink.targetFieldType, metaData.getInternalGetter(backlink.targetField), EnumSet.of(Modifier.PUBLIC))
                endMethod()
            }

            endType()
            close()
        }
    }
}
