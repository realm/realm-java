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

package io.realm.transformer

import io.realm.annotations.Ignore
import javassist.*
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ConstPool
import javassist.bytecode.Opcode
import javassist.bytecode.annotation.Annotation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

class ByteCodeModifierTest {

    @Test
    fun addRealmAccessors() {
        // Generate an empty class
        val classPool = ClassPool.getDefault()
        val ctClass = classPool.makeClass("testClass")

        // Add a field
        val ctField = CtField(CtClass.intType, "age", ctClass)
        ctClass.addField(ctField)

        // The accessors are added
        BytecodeModifier.addRealmAccessors(ctClass)

        // The accessors are generated
        val ctMethods = ctClass.declaredMethods
        val methodNames = ctMethods.map { it.name }
        assertTrue(methodNames.contains("realmGet\$age"))
        assertTrue(methodNames.contains("realmSet\$age"))

        // The accessors are public
        ctMethods.forEach {
            assertTrue(it.modifiers == Modifier.PUBLIC)
        }
    }

    // https://github.com/realm/realm-java/issues/3469
    @Test
    fun addRealmAccessors_duplicateSetter() {
        // Generate an empty class
        val classPool = ClassPool.getDefault()
        val ctClass = classPool.makeClass("testClass")

        // Add a field
        val ctField = CtField(CtClass.intType, "age", ctClass)
        ctClass.addField(ctField)

        // Add a setter
        val setter = CtNewMethod.setter("realmSet\$age", ctField)
        ctClass.addMethod(setter)

        // addRealmAccessors is called
        BytecodeModifier.addRealmAccessors(ctClass)

        // a getter for the field is generated
        val ctMethods = ctClass.declaredMethods
        val methodNames = ctMethods.map { it.name }
        assertTrue(methodNames.contains("realmGet\$age"))

        // the setter is not changed
        assertTrue(ctMethods.find { it.name == "realmSet\$age" } == setter)

        // accessors are public
        ctMethods.forEach {
            assertTrue(it.modifiers == Modifier.PUBLIC)
        }
    }

    // https://github.com/realm/realm-java/issues/3469
    @Test
    fun addRealmAccessors_duplicateGetter() {
        // Generate an empty class
        val classPool = ClassPool.getDefault()
        val ctClass = classPool.makeClass("testClass")

        // Add a field
        val ctField = CtField(CtClass.intType, "age", ctClass)
        ctClass.addField(ctField)

        // Add a getter
        val setter = CtNewMethod.setter("realmGet\$age", ctField)
        ctClass.addMethod(setter)

        // addRealmAccessors is called
        BytecodeModifier.addRealmAccessors(ctClass)

        // a setter for the field is generated
        val ctMethods = ctClass.declaredMethods
        val methodNames = ctMethods.map { it.name }
        assertTrue(methodNames.contains("realmSet\$age"))

        // the getter is not changed
        assertTrue(ctMethods.find { it.name == "realmGet\$age" } == setter)

        // accessors are public
        ctMethods.forEach {
            assertTrue(it.modifiers == Modifier.PUBLIC)
        }
    }

    @Test
    fun addRealmAccessors_ignoreAnnotation() {
        // Generate an empty class
        val classPool = ClassPool.getDefault()
        val ctClass = classPool.makeClass("testClass")
        val constPool = ConstPool("TestClass")

        // Add a field with @Ignore
        val ctField = CtField(CtClass.intType, "age", ctClass)
        ctClass.addField(ctField)
        val attr = AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag)
        val ignoreAnnotation = Annotation(Ignore::class.java.name, constPool)
        attr.addAnnotation(ignoreAnnotation)
        ctField.fieldInfo.addAttribute(attr)

        // Try to add the accessor
        BytecodeModifier.addRealmAccessors(ctClass)

        // the accessor should not be generated
        // a setter for the field is generated
        val ctMethods = ctClass.declaredMethods
        val methodNames = ctMethods.map { it.name }
        assertFalse(methodNames.contains("realmSet\$age"))
        assertFalse(methodNames.contains("realmGet\$age"))
    }

    @Test
    fun userRealmAccessors() {
        // Generate an empty class
        val classPool = ClassPool.getDefault()
        val ctClass = classPool.makeClass("testClass")

        // Add a field
        val ctField = CtField(CtClass.intType, "age", ctClass)
        ctClass.addField(ctField)

        // Add a method that uses such field
        val ctMethod = CtNewMethod.make("public boolean canDrive() { return this.age >= 18; }", ctClass)
        ctClass.addMethod(ctMethod)

        // Realm accessors are called
        BytecodeModifier.addRealmAccessors(ctClass)

        // the field use is replaced by the accessor
        BytecodeModifier.useRealmAccessors(classPool, ctClass, listOf(ctField))

        // the field is not used and getter is called in the method
        assertTrue(!isFieldRead(ctMethod) && hasMethodCall(ctMethod))
    }

    fun userRealmAccessors_fieldAccessConstructorIsTransformed() {
        // Generate an empty class
        val classPool = ClassPool.getDefault()
        val ctClass = classPool.makeClass("testClass")
        val constPool = ConstPool("TestClass")

        // Add a field with @Ignore
        val ctField = CtField(CtClass.intType, "age", ctClass)
        ctClass.addField(ctField)

        // Add a method sets such field
        val ctMethod = CtNewMethod.make("private void setupAge(int age) { this.age = age; }", ctClass)
        ctClass.addMethod(ctMethod)

        // Add a default constructor that uses the method
        val ctDefaultConstructor = CtNewConstructor.make("public TestClass() { int myAge = this.age; }", ctClass)
        ctClass.addConstructor(ctDefaultConstructor)

        // Add a non-default constructor that uses the method
        val ctNonDefaultConstructor = CtNewConstructor.make("public TestClass(TestClass other) { int otherAge = other.age; }", ctClass)
        ctClass.addConstructor(ctNonDefaultConstructor)

        // Realm accessors are added
        BytecodeModifier.addRealmAccessors(ctClass)

        // the field use is replaced by the accessor
        BytecodeModifier.useRealmAccessors(classPool, ctClass, listOf(ctField))

        // the field is not used in the method anymore
        assertTrue(!isFieldRead(ctDefaultConstructor)
                && hasMethodCall(ctDefaultConstructor)
                && !isFieldRead(ctNonDefaultConstructor)
                && hasMethodCall(ctNonDefaultConstructor))
    }

    private fun isFieldRead(behavior: CtBehavior): Boolean {
        val methodInfo = behavior.methodInfo
        val codeAttribute = methodInfo.codeAttribute

        val it = codeAttribute.iterator()
        var index = 0;
        while (it.hasNext()) {
            val op: Int = it.byteAt(index)
            index = it.next()
            if (op == Opcode.GETFIELD) {
                return true;
            }
        }
        return false
    }

    private fun hasMethodCall(behavior: CtBehavior): Boolean {
        val methodInfo = behavior.methodInfo
        val codeAttribute = methodInfo.codeAttribute

        val it = codeAttribute.iterator()
        var index = 0;
        while (it.hasNext()) {
            val op: Int = it.byteAt(index)
            index = it.next()
            if (op == Opcode.INVOKEVIRTUAL) {
                return true;
            }
        }
        return false
    }

}
