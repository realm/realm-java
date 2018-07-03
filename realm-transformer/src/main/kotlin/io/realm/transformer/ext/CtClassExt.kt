package io.realm.transformer.ext

import javassist.CtClass
import javassist.NotFoundException
import javassist.bytecode.ClassFile


/**
 * Returns {@code true} if 'clazz' is considered a subtype of 'superType'.
 *
 * This function is different than {@link CtClass#subtypeOf(CtClass)} in the sense
 * that it will never crash even if classes are missing from the class pool, instead
 * it will just return {@code false}.
 *
 * This e.g. happens with RxJava classes which are optional, but JavaAssist will try
 * to load them and then crash.
 *
 * @param typeToCheckAgainst the type we want to check against
 * @return `true` if `clazz` is a subtype of `typeToCheckAgainst`, `false` otherwise.
 */
fun CtClass.safeSubtypeOf(typeToCheckAgainst: CtClass): Boolean {
    val typeToCheckAgainstQualifiedName: String = typeToCheckAgainst.name
    if (this == typeToCheckAgainst || this.name.equals(typeToCheckAgainstQualifiedName)) {
        return true
    }

    val file: ClassFile = this.classFile2

    // Check direct super class
    val superName: String? = file.superclass
    if (superName.equals(typeToCheckAgainstQualifiedName)) {
        return true
    }

    // Check direct interfaces
    val ifs: Array<String> = file.interfaces
    ifs.forEach {
        if (it == typeToCheckAgainstQualifiedName) {
            return true
        }
    }

    // Check other inherited super classes
    if (superName != null) {
        var nextSuper: CtClass
        try {
            nextSuper = classPool.get(superName)
            if (nextSuper.safeSubtypeOf(typeToCheckAgainst)) {
                return true
            }
        } catch (ignored: NotFoundException) {
        }
    }

    // Check other inherited interfaces
    ifs.forEach { interfaceName ->
        try {
            val interfaceClass: CtClass = classPool.get(interfaceName)
            if (interfaceClass.safeSubtypeOf(typeToCheckAgainst)) {
                return true
            }
        } catch (ignored: NotFoundException) {
        }
    }

    return false
}
