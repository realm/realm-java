package buildtransformer.visitors

import org.objectweb.asm.*
import org.objectweb.asm.tree.MethodNode

/**
 * Visitor that is the main entry point for analyzing class files.
 * It will remove any class, method or field annotation with the provided annotation
 */
class AnnotationBasedClassRemover(private val annotation: String, classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM5, classVisitor) {





    // Ignore all classes marked with the annotation
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return if (descriptor == annotation) {
            null
        } else {
            super.visitAnnotation(descriptor, visible)
        }
    }

    override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor {
        val parentVisitor: FieldVisitor = super.visitField(access, name, descriptor, signature, value)
        return AnnotationBasedFieldRemover(annotation, api, parentVisitor)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val parentVisitor: MethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
//        return AnnotationBasedMethodRemover(annotation, api, parentVisitor)
        val method: MethodNode = object: MethodNode() {
            override fun visitEnd() {



                super.visitEnd()
            }
        }

        return method
    }
}