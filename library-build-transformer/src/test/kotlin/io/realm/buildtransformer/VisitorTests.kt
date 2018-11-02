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
package io.realm.buildtransformer

import io.realm.buildtransformer.asm.ClassPoolTransformer
import io.realm.buildtransformer.ext.packageHierarchyRootDir
import io.realm.buildtransformer.ext.shouldBeDeleted
import io.realm.buildtransformer.testclasses.*
import io.realm.internal.annotations.CustomAnnotation
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.lang.reflect.Method
import kotlin.reflect.KClass

class VisitorTests {

    private lateinit var classLoader: DynamicClassLoader
    private val qualifiedAnnotationName = "io.realm.internal.annotations.ObjectServer"

    @Before
    fun setUp () {
        classLoader = DynamicClassLoader(this::class.java.classLoader)
    }

    @Test
    fun removeFields() {
        val c: Class<SimpleTestFields> = modifyClass(SimpleTestFields::class)
        assetDefaultConstructorExists(c)
        assertFieldExists("field2", c)
        assertFieldRemoved("field1", c)
        assertTrue(c.getField("field2").isAnnotationPresent(CustomAnnotation::class.java))
    }

    @Test
    fun removeMethods() {
        val c: Class<SimpleTestMethods> = modifyClass(SimpleTestMethods::class)
        assetDefaultConstructorExists(c)
        assertMethodExists(c, "bar", emptyArray());
        assertMethodExists(c, "foo1", arrayOf(String::class.java, String::class.java))
        assertMethodRemoved(c, "foo", emptyArray());
        assertMethodRemoved(c, "foo1", arrayOf(String::class.java))
    }

    @Test
    fun removeTopLevelClass() {
        try {
            modifyClass(SimpleTestClass::class)
            fail()
        } catch(e: IllegalStateException) {
            assertTrue(e.message?.contains("Class pool does not contain") == true)
        }
    }

    @Test
    fun removeClassIfSuperClassIsAnnotated() {
        try {
            modifyClass(SubClass::class, setOf(SubClass::class, SuperClass::class))
            fail()
        } catch(e: IllegalStateException) {
            assertTrue(e.message?.contains("Class pool does not contain") == true)
        }
    }

    @Test
    fun removeInnerClasses() {
        // The reflection API does not make it possible to find inner classes, so we need to inspect the bytecode
        // instead. We do this by checking the output of the transformer which will only output files modified and
        // not classes deleted.
        val inputClasses: MutableSet<File> = mutableSetOf()
        setOf<KClass<*>>(
            NestedTestClass::class,
            NestedTestClass.InnerClass::class,
            NestedTestClass.StaticInnerClass::class,
            NestedTestClass.Enum::class,
            NestedTestClass.Interface::class
        ).forEach { inputClasses.add(getClassFile(it)) }
        val transformer = ClassPoolTransformer(qualifiedAnnotationName, inputClasses)
        val outputFiles: Set<File> = transformer.transform()
        assertEquals(1, outputFiles.filter { !it.shouldBeDeleted }.size) // Only top level file is saved.
        assertTrue(outputFiles.first().name.endsWith("NestedTestClass.class"))
    }

    private fun assetDefaultConstructorExists(clazz: Class<*>) {
        clazz.getConstructor()
    }

    private fun assertFieldRemoved(fieldName: String, clazz: Class<*>) {
        try {
            clazz.getField(fieldName)
            fail("Field $fieldName has not been removed")
        } catch (e: NoSuchFieldException) {
        }
    }

    private fun assertFieldExists(fieldName: String, clazz: Class<*>) {
        clazz.getField(fieldName)
    }

    private fun assertMethodRemoved(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>) {
        try {
            clazz.getMethod(methodName, *parameterTypes);
            fail("Method $methodName has not been removed");
        } catch (e: NoSuchMethodException) {
        }
    }

    private fun assertMethodExists(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>) {
        val method: Method = clazz.getMethod(methodName, *parameterTypes) // Will throw exception if it doesn't exists
        assertNotNull(method)
    }

    private fun <T: Any> modifyClass(clazz: KClass<T>): Class<T> {
        return this.modifyClass(clazz, setOf(clazz))
    }

    private fun <T: Any> modifyClass(clazz: KClass<T>, pool: Set<KClass<*>>): Class<T> {
        val inputClasses: MutableSet<File> = mutableSetOf()
        pool.forEach { inputClasses.add(getClassFile(it)) }
        val transformer = ClassPoolTransformer(qualifiedAnnotationName, inputClasses)
        val outputFiles: Set<File> = transformer.transform().filter { !it.shouldBeDeleted }.toSet()
        @Suppress("UNCHECKED_CAST")
        return classLoader.loadClass(clazz.java.name, outputFiles) as Class<T>
    }

    private fun getClassFile(clazz: KClass<*>): File {
        return getClassFile(clazz.java)
    }

    private fun getClassFile(clazz: Class<*>): File {
        val filePath = "${clazz.name.replace(".", "/")}.class"
        val file = File(classLoader.getResource(filePath).file)
        // Extension properties must be initialized before they can be read
        file.shouldBeDeleted = false
        file.packageHierarchyRootDir = ""
        return file
    }
}
