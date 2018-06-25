package io.realm.buildtransformer

import io.realm.DynamicClassLoader
import io.realm.buildtransformer.asm.ClassTransformer
import io.realm.classes.SimpleTestClass
import io.realm.classes.SimpleTestFields
import io.realm.classes.SimpleTestMethods
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.fail

class VisitorTests {

    private lateinit var classLoader: DynamicClassLoader
    private val qualifiedAnnotationName = "io.realm.internal.annotations.ObjectServer"

    @Before
    fun setUp () {
        classLoader = DynamicClassLoader(this::class.java.classLoader)
    }

    @Test
    fun removeFields() {
        val c: Class<*> = modifyClass(SimpleTestFields::class)
        assertFieldRemoved("field1", c)
        assertFieldExists("field2", c)
    }

    @Test
    fun removeMethods() {
        val c: Class<*> = modifyClass(SimpleTestMethods::class)
        assertMethodRemoved("foo", c)
        assertMethodExists("bar", c)
    }

    @Test
    fun removeTopLevelClass() {
        try {
            modifyClass(SimpleTestClass::class)
            fail()
        } catch(ignore: ClassFormatError) {
        }
    }


    private fun assertFieldRemoved(fieldName: String, clazz: Class<*>) {
        try {
            clazz.getField(fieldName)
            fail("Field $fieldName has not been removed");
        } catch (e: NoSuchFieldException) {
        }
    }

    private fun assertFieldExists(fieldName: String, clazz: Class<*>) {
        clazz.getField(fieldName)
    }

    private fun assertMethodRemoved(methodName: String, clazz: Class<*>) {
        try {
            clazz.getMethod(methodName)
            fail("Method $methodName has not been removed");
        } catch (e: NoSuchMethodException) {
        }
    }

    private fun assertMethodExists(methodName: String, clazz: Class<*>) {
        clazz.getMethod(methodName) // Will throw exception if it doesn't
    }

    private fun modifyClass(clazz: KClass<*>): Class<*> {
        val file = getClassFile(clazz)
        val transformer = ClassTransformer(file, qualifiedAnnotationName)
        val modifiedClassData: ByteArray = transformer.transform()
        val c: Class<*> = classLoader.loadClass(clazz.java.name, modifiedClassData)
        return c
    }

    private fun getClassFile(clazz: KClass<*>): File {
        val filePath = "${clazz.java.name.replace(".", "/")}.class"
        return File(classLoader.getResource(filePath).file)
    }
}