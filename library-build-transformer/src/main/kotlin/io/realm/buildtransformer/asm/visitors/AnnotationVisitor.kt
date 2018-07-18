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
 * parse and is required for correctly identifying them in the 2nd parse before any byte code is
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

