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

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtNewMethod
import javassist.bytecode.CodeIterator
import javassist.bytecode.Opcode
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
        methodNames.contains('realmGetter$age')
        methodNames.contains('realmSetter$age')

        and: 'the accessors are public'
        ctMethods.each {
            it.getModifiers() == Modifier.PUBLIC
        }
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
        BytecodeModifier.useRealmAccessors(ctClass, [ctField])

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
}
