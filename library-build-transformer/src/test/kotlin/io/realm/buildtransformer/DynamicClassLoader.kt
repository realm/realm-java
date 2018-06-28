package io.realm.buildtransformer

import java.io.File

/**
 * Custom ClassLoader that can be used to dynamically load a class that have been dynamically modified, i.e. we
 * load it using the ByteArray that represents it.
 */
class DynamicClassLoader(parentLoader: ClassLoader) : ClassLoader(parentLoader) {

    fun loadClass(qualifiedClassName: String, pool: Set<File>): Class<*> {
        val classFile: File = pool.find {
            it.absolutePath.endsWith("${qualifiedClassName.replace(".", "/")}.class")
        } ?: throw IllegalStateException("Class pool does not contain: $qualifiedClassName")

        val classBytes: ByteArray = classFile.readBytes()
        return defineClass(qualifiedClassName, classBytes, 0, classBytes.size)
    }
}