package io.realm.buildtransformer.visitors

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

class AnnotationBasedMethodRemover(private val annotation: String, api: Int, parentVisitor: MethodVisitor)
    : MethodVisitor(api, parentVisitor) {

    // Ignore the method if it annotated with the given annotation
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return if (annotation == descriptor) {
            null
        } else {
            super.visitAnnotation(descriptor, visible)
        }
    }
}