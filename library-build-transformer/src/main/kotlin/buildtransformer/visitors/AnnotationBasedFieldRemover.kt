package buildtransformer.visitors

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor

class AnnotationBasedFieldRemover(private val annotation: String, api: Int, parentVisitor: FieldVisitor)
    : FieldVisitor(api, parentVisitor) {

    // Ignore the field if it annotated with the given annotation
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return if (annotation == descriptor) {
            null
        } else {
            super.visitAnnotation(descriptor, visible)
        }
    }
}