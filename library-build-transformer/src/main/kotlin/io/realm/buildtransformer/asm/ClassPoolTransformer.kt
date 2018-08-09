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
package io.realm.buildtransformer.asm

import io.realm.buildtransformer.ByteCodeMethodName
import io.realm.buildtransformer.ByteCodeTypeDescriptor
import io.realm.buildtransformer.QualifiedName
import io.realm.buildtransformer.asm.visitors.AnnotatedCodeStripVisitor
import io.realm.buildtransformer.asm.visitors.AnnotationVisitor
import io.realm.buildtransformer.ext.shouldBeDeleted
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File

/**
 * Transformer that will transform a pool of classes by removing all classes, methods and fields annotated with
 * a given annotation.
 *
 * It does so in 2 passes using the ASM Visitor API. The first pass will gather metadata about the class hierarchy,
 * the 2nd pass will do the actual transform. The ASM Tree API was considered as well, but it does not provide
 * access to referenced classes easily, so two passes would be required here as well and the Visitor API is faster and
 * requires less memory.
 */
class ClassPoolTransformer(annotationQualifiedName: QualifiedName, private val inputClasses: Set<File>) {

    private val annotationDescriptor: String = createDescriptor(annotationQualifiedName)

    /**
     * Transform files
     *
     * @return All input files, both those that have been modified and those that have not.
     */
    fun transform(): Set<File> {
        val (markedClasses, markedMethods) = pass1()
        return pass2(markedClasses, markedMethods)
    }

    /**
     * Pass 1: Collect all classes, interfaces and enums that contain the given annotation. This include both top-level
     * and inner types.
     */
    private fun pass1(): Pair<Set<String>, Map<ByteCodeTypeDescriptor, Set<ByteCodeMethodName>>> {
        val metadataCollector = AnnotationVisitor(annotationDescriptor)
        inputClasses.forEach {
            it.inputStream().use {
                val classReader = ClassReader(it)
                classReader.accept(metadataCollector, 0)
            }
        }
        return Pair(metadataCollector.annotatedClasses, metadataCollector.annotatedMethods)
    }

    /**
     * Pass 2: Remove methods and fields marked with the annotation. Classes that are removed
     * are instead marked for deletion as deleting the File is the responsibility of the
     * transform API.
     */
    private fun pass2(markedClasses: Set<String>, markedMethods: Map<ByteCodeTypeDescriptor, Set<ByteCodeMethodName>>): Set<File> {
        inputClasses.forEach { classFile ->
            var result = ByteArray(0)
            if (!classFile.shouldBeDeleted) { // Respect previously set delete flag, so avoid doing any work
                classFile.inputStream().use { inputStream ->
                    val writer = ClassWriter(0) // We don't modify methods so no reason to re-calculate method frames
                    val classRemover = AnnotatedCodeStripVisitor(annotationDescriptor, markedClasses, markedMethods, writer)
                    val reader = ClassReader(inputStream)
                    reader.accept(classRemover, 0)
                    result = if (classRemover.deleteClass) ByteArray(0) else writer.toByteArray()
                }
                if (result.isNotEmpty()) {
                    classFile.outputStream().use { outputStream -> outputStream.write(result) }
                } else {
                    classFile.shouldBeDeleted = true
                }
            }
        }
        return inputClasses
    }

    /**
     * Creates the descriptor used by ASM to identify types.
     */
    private fun createDescriptor(qualifiedName: String): String {
        return "L${qualifiedName.replace(".", "/")};"
    }
}
