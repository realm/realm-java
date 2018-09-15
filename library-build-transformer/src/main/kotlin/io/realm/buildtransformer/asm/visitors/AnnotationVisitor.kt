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
package io.realm.buildtransformer.asm.visitors;

import io.realm.buildtransformer.ByteCodeMethodName
import io.realm.buildtransformer.ByteCodeTypeDescriptor
import io.realm.buildtransformer.logger
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * ClassVisitor that gather all classes and methods with the given annotation. This is the first
 * pass and is required for correctly identifying them in the 2nd pass before any byte code is
 * written.
 */
class AnnotationVisitor(private val annotationDescriptor: String) : ClassVisitor(Opcodes.ASM6) {

    val annotatedClasses: MutableSet<ByteCodeTypeDescriptor> = mutableSetOf()
    val annotatedMethods: MutableMap<ByteCodeTypeDescriptor, MutableSet<ByteCodeMethodName>> = mutableMapOf()
    private var internalQualifiedName: String = ""
    private val annotatedMethodsInClass = mutableSetOf<ByteCodeMethodName>()

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        internalQualifiedName = name!!
        annotatedMethods[internalQualifiedName] = annotatedMethodsInClass
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        if (descriptor == annotationDescriptor) {
            annotatedClasses.add(internalQualifiedName)
        }
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val parentVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return object: MethodVisitor(api, parentVisitor) {
            override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
                if (descriptor == annotationDescriptor) {
                    annotatedMethodsInClass.add(name!!)
                }
                return super.visitAnnotation(descriptor, visible)
            }
        }
    }

    override fun visitEnd() {
        annotatedMethods[internalQualifiedName] = annotatedMethodsInClass
        super.visitEnd()
    }

}
