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

package io.realm.gradle

import javassist.CannotCompileException
import javassist.CtBehavior
import javassist.CtClass
import javassist.CtField
import javassist.CtNewMethod
import javassist.NotFoundException
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BytecodeModifier {
    public static void addRealmAccessors(CtClass clazz) {
        Logger logger = LoggerFactory.getLogger('realm-logger')
        logger.info "  Realm: Adding accessors to ${clazz.simpleName}"
        clazz.declaredFields.each { CtField field ->
            try {
                clazz.getDeclaredMethod("realmGetter\$${field.name}")
            } catch (NotFoundException ignored) {
                clazz.addMethod(CtNewMethod.getter("realmGetter\$${field.name}", field))
            }
            try {
                clazz.getDeclaredMethod("realmSetter\$${field.name}")
            } catch (NotFoundException ignored) {
                clazz.addMethod(CtNewMethod.setter("realmSetter\$${field.name}", field))
            }
        }
    }

    public static void useRealmAccessors(CtClass clazz, List<CtField> managedFields) {
        Logger logger = LoggerFactory.getLogger('realm-logger')
        clazz.getDeclaredBehaviors().each { behavior ->
            logger.info "    Behavior: ${behavior.name}"
            if (!behavior.name.startsWith('realmGetter$') && !behavior.name.startsWith('realmSetter$')) {
                behavior.instrument(new ExpressionEditor(managedFields, clazz, behavior))
            }
        }
    }

    private static class ExpressionEditor extends ExprEditor {
        List<CtField> managedFields
        CtClass ctClass
        CtBehavior behavior

        ExpressionEditor(List<CtField> managedFields, CtClass ctClass, CtBehavior behavior) {
            this.managedFields = managedFields
            this.ctClass = ctClass
            this.behavior = behavior
        }

        @Override
        void edit(FieldAccess fieldAccess) throws CannotCompileException {
            Logger logger = LoggerFactory.getLogger('realm-logger')
            try {
                logger.info "      Field being accessed: ${fieldAccess.className}.${fieldAccess.fieldName}"
                def flag = false
                managedFields.each {
                    if (fieldAccess.className.equals(it.declaringClass.name) && fieldAccess.fieldName.equals(it.name)) {
                        flag = true
                    }
                }
                if (flag) {
                    logger.info "        Realm: Manipulating ${ctClass.simpleName}.${behavior.name}(): ${fieldAccess.fieldName}"
                    logger.info "        Methods: ${ctClass.declaredMethods}"
                    def fieldName = fieldAccess.fieldName
                    if (fieldAccess.isReader()) {
                        fieldAccess.replace('$_ = $0.realmGetter$' + fieldName + '();')
                    } else if (fieldAccess.isWriter()) {
                        fieldAccess.replace('$0.realmSetter$' + fieldName + '($1);')
                    }
                }
            } catch (NotFoundException ignored) {
            }
        }
    }
}
