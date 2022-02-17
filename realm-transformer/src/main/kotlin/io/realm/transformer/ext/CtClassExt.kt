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
