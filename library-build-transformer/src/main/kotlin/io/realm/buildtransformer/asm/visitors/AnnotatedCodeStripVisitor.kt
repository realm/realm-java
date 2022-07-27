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
package io.realm.buildtransformer.asm.visitors

import io.realm.buildtransformer.ByteCodeMethodName
import io.realm.buildtransformer.ByteCodeTypeDescriptor
import io.realm.buildtransformer.FieldName
import io.realm.buildtransformer.logger
import org.objectweb.asm.*
import org.objectweb.asm.AnnotationVisitor

/**
 * Visitor that will remove all classes, methods and fields annotated with given annotation.
 * Doing this requires a pre-processing step performed by the [AnnotationVisitor].
 */
class AnnotatedCodeStripVisitor(private val annotationDescriptor: String,
                                private val markedClasses: Set<String>,
                                private val markedMethods: Map<ByteCodeTypeDescriptor, Set<ByteCodeMethodName>>,
                                private val markedFields: Map<ByteCodeTypeDescriptor, Set<FieldName>>,
                                classWriter: ClassVisitor) : ClassVisitor(Opcodes.ASM7_EXPERIMENTAL, classWriter) {

    var deleteClass: Boolean = false
    private lateinit var markedMethodsInClass: Set<ByteCodeMethodName>
    private lateinit var markedFieldsInClass: Set<FieldName>

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        // Only process this class if it or its super class doesn't have the given annotation
        markedMethodsInClass = markedMethods[name!!]!!
        markedFieldsInClass = markedFields[name]!!
        deleteClass = (markedClasses.contains(name) || markedClasses.contains(superName))
        if (!deleteClass) {
            super.visit(version, access, name, signature, superName, interfaces)
        } else {
            logger.debug("Removing top level class: $name")
        }
    }

    // Remove INNERCLASS definitions from the bytecode in the top level class. It isn't clear if
    // these are used by any relevant API's, but better remove them just in case.
    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
        if (!markedClasses.contains(name)) {
            super.visitInnerClass(name, outerName, innerName, access)
        } else {
            logger.debug("Removing inner class description: $name")
        }
    }

    override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor? {
        return if (!markedFieldsInClass.contains(name)) {
            super.visitField(access, name, descriptor, signature, value)
        } else {
            logger.debug("Removing field: $name")
            null
        }
    }

    override fun visitMethod(access: Int, name: ByteCodeMethodName?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        return if (!markedMethodsInClass.contains(name + descriptor)) {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        } else {
            logger.debug("Removing method: $name $descriptor")
            null
        }
    }

}