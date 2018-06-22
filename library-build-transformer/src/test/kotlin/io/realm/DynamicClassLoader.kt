package io.realm

/**
 * Custom ClassLoader that can be used to dynamically load a class that have been dynamically modified, i.e. we
 * load it using the ByteArray that represents it.
 */
class DynamicClassLoader(parentLoader: ClassLoader) : ClassLoader(parentLoader) {

    fun loadClass(qualifiedName: String, classBytes: ByteArray): Class<*> {
        return defineClass(qualifiedName, classBytes, 0, classBytes.size)
    }
}