/*
 * Copyright 2016 Realm Inc.
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
import javassist.bytecode.CodeIterator
import javassist.bytecode.ConstPool
import javassist.bytecode.Opcode
import javassist.bytecode.annotation.Annotation
import spock.lang.Specification

import java.lang.reflect.Modifier

class BytecodeModifierTest extends Specification {
    def "AddRealmAccessors"() {
        setup: 'generate an empty class'
        def classPool = ClassPool.getDefault()
        def ctClass = classPool.makeClass('testClass')

        and: 'add a field'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)

        when: 'the accessors are added'
        BytecodeModifier.addRealmAccessors(ctClass)

        then: 'the accessors are generated'
        def ctMethods = ctClass.getDeclaredMethods()
        def methodNames = ctMethods.name
        methodNames.contains('realmGet$age')
        methodNames.contains('realmSet$age')

        and: 'the accessors are public'
        ctMethods.each {
            it.getModifiers() == Modifier.PUBLIC
        }
    }

    // https://github.com/realm/realm-java/issues/3469
    def "AddRealmAccessors_duplicateSetter"() {
        setup: 'generate an empty class'
        def classPool = ClassPool.getDefault()
        def ctClass = classPool.makeClass('testClass')

        and: 'add a field'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)

        and: 'add a setter'
        def setter = CtNewMethod.setter('realmSet$age', ctField)
        ctClass.addMethod(setter)

        when: 'addRealmAccessors is called'
        BytecodeModifier.addRealmAccessors(ctClass)

        then: 'a getter for the field is generated'
        def ctMethods = ctClass.getDeclaredMethods()
        def methodNames = ctMethods.name
        methodNames.contains('realmGet$age')

        and: 'the setter is not changed'
        ctMethods.find {it.name.equals('realmSet$age')} == setter

        and: 'the accessors are public'
        ctMethods.each {
            it.getModifiers() == Modifier.PUBLIC
        }
    }

    // https://github.com/realm/realm-java/issues/3469
    def "AddRealmAccessors_duplicateGetter"() {
        setup: 'generate an empty class'
        def classPool = ClassPool.getDefault()
        def ctClass = classPool.makeClass('testClass')

        and: 'add a field'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)

        and: 'add a getter'
        def getter = CtNewMethod.getter('realmGet$age', ctField)
        ctClass.addMethod(getter)

        when: 'addRealmAccessors is called'
        BytecodeModifier.addRealmAccessors(ctClass)

        then: 'a setter for the field is generated'
        def ctMethods = ctClass.getDeclaredMethods()
        def methodNames = ctMethods.name
        methodNames.contains('realmSet$age')

        and: 'the getter is not changed'
        ctMethods.find {it.name.equals('realmGet$age')} == getter

        and: 'the accessors are public'
        ctMethods.each {
            it.getModifiers() == Modifier.PUBLIC
        }
    }

    def "AddRealmAccessors_IgnoreAnnotation"() {
        setup: 'generate an empty class'
        def classPool = ClassPool.getDefault()
        def ctClass = classPool.makeClass('TestClass')
        def constPool = new ConstPool('TestClass')

        and: 'add a field with @Ignore'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)
        def attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag)
        def ignoreAnnotation = new Annotation(Ignore.class.name, constPool)
        attr.addAnnotation(ignoreAnnotation)
        ctField.fieldInfo.addAttribute(attr)

        when: 'Try to add the accessor'
        BytecodeModifier.addRealmAccessors(ctClass)

        then: 'the accessor should not be generated'
        def ctMethods = ctClass.getDeclaredMethods()
        def methodNames = ctMethods.name
        !methodNames.contains('realmGet$age')
        !methodNames.contains('realmSet$age')
    }

    def "UseRealmAccessors"() {
        setup: 'generate an empty class'
        def classPool = ClassPool.getDefault()
        def ctClass = classPool.makeClass('TestClass')

        and: 'add a field'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)

        and: 'add a method that uses such field'
        def ctMethod = CtNewMethod.make('public boolean canDrive() { return this.age >= 18; }', ctClass)
        ctClass.addMethod(ctMethod)

        and: 'realm accessors are added'
        BytecodeModifier.addRealmAccessors(ctClass)

        when: 'the field use is replaced by the accessor'
        BytecodeModifier.useRealmAccessors(classPool, ctClass, [ctField])

        then: 'the field is not used and getter is called in the method '
        !isFieldRead(ctMethod) && hasMethodCall(ctMethod)
    }

    def "UseRealmAccessors_fieldAccessConstructorIsTransformed"() {
        setup: 'generate an empty class'
        def classPool = ClassPool.getDefault()
        def ctClass = classPool.makeClass('TestClass')

        and: 'add a field'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)

        and: 'add a method that sets such field'
        def ctMethod = CtNewMethod.make('private void setupAge(int age) { this.age = age; }', ctClass)
        ctClass.addMethod(ctMethod)

        and: 'add a default constructor that uses the method'
        def ctDefaultConstructor = CtNewConstructor.make('public TestClass() { int myAge = this.age; }', ctClass)
        ctClass.addConstructor(ctDefaultConstructor)

        and: 'add a non-default constructor that uses the method'
        def ctNonDefaultConstructor = CtNewConstructor.make('public TestClass(TestClass other) { int otherAge = other.age; }', ctClass)
        ctClass.addConstructor(ctNonDefaultConstructor)

        and: 'realm accessors are added'
        BytecodeModifier.addRealmAccessors(ctClass)

        when: 'the field use is replaced by the accessor'
        BytecodeModifier.useRealmAccessors(classPool, ctClass, [ctField])

        then: 'the field is not used in the method anymore'
        !isFieldRead(ctDefaultConstructor) && hasMethodCall(ctDefaultConstructor) &&
                !isFieldRead(ctNonDefaultConstructor) && hasMethodCall(ctNonDefaultConstructor)
    }

    private static def isFieldRead(CtBehavior behavior) {
        def methodInfo = behavior.getMethodInfo()
        def codeAttribute = methodInfo.getCodeAttribute()

        for (CodeIterator ci = codeAttribute.iterator(); ci.hasNext();) {
            int index = ci.next()
            int op = ci.byteAt(index)
            if (op == Opcode.GETFIELD) {
                return true
            }
        }
        return false
    }

    private static def hasMethodCall(CtBehavior behavior) {
        def methodInfo = behavior.getMethodInfo()
        def codeAttribute = methodInfo.getCodeAttribute()

        for (CodeIterator ci = codeAttribute.iterator(); ci.hasNext();) {
            int index = ci.next()
            int op = ci.byteAt(index)
            if (op == Opcode.INVOKEVIRTUAL) {
                return true
            }
        }
        return false
    }
}
