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
        def ctClass = classPool.makeClass('testClass')

        and: 'add a field'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)

        and: 'add a method that uses such field'
        def ctMethod = CtNewMethod.make('public boolean canDrive() { return this.age >= 18; }', ctClass)
        ctClass.addMethod(ctMethod)

        and: 'realm accessors are added'
        BytecodeModifier.addRealmAccessors(ctClass)

        when: 'the field use is replaced by the accessor'
        BytecodeModifier.useRealmAccessors(ctClass, [ctField], [])

        then: 'the field is not used in the method anymore'
        def methodInfo = ctMethod.getMethodInfo()
        def codeAttribute = methodInfo.getCodeAttribute()
        def fieldIsUsed = false
        for (CodeIterator ci = codeAttribute.iterator(); ci.hasNext();) {
            int index = ci.next();
            int op = ci.byteAt(index);
            if (op == Opcode.GETFIELD) {
                fieldIsUsed = true
            }
        }
        !fieldIsUsed
    }

    def "UseRealmAccessorsInNonDefaultConstructor"() {
        setup: 'generate an empty class'
        def classPool = ClassPool.getDefault()
        def ctClass = classPool.makeClass('TestClass')

        and: 'add a field'
        def ctField = new CtField(CtClass.intType, 'age', ctClass)
        ctClass.addField(ctField)

        and: 'add a method that sets such field'
        def ctMethod = CtNewMethod.make('private void setupAge(int age) { this.age = age; }', ctClass)
        ctClass.addMethod(ctMethod)

        and: 'add a constructor that uses the method'
        def ctConstructor = CtNewConstructor.make('public TestClass(int age) { setupAge(age); }', ctClass)
        ctClass.addConstructor(ctConstructor)

        and: 'realm accessors are added'
        BytecodeModifier.addRealmAccessors(ctClass)

        when: 'the field use is replaced by the accessor'
        BytecodeModifier.useRealmAccessors(ctClass, [ctField], [])

        then: 'the field is not used in the method anymore'
        def methodInfo = ctMethod.getMethodInfo()
        def codeAttribute = methodInfo.getCodeAttribute()
        def fieldIsUsed = false
        for (CodeIterator ci = codeAttribute.iterator(); ci.hasNext();) {
            int index = ci.next();
            int op = ci.byteAt(index);
            if (op == Opcode.PUTFIELD) {
                fieldIsUsed = true
            }
        }
        !fieldIsUsed
    }
}
