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

import javassist.*
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import javassist.expr.MethodCall
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class encapsulates the bytecode manipulation code needed to transform model classes
 * and the classes using them.
 */
class BytecodeModifier {

    private static final Logger logger = LoggerFactory.getLogger('realm-logger')

    /**
     * Adds Realm specific accessors to a model class.
     *
     * @param clazz the CtClass to add accessors to.
     * All the declared fields will be associated with a getter and a setter.
     */
    public static void addRealmAccessors(CtClass clazz) {
        logger.info "  Realm: Adding accessors to ${clazz.simpleName}"
        def methods = clazz.getDeclaredMethods()*.name
        clazz.declaredFields.each { CtField field ->
            if (!Modifier.isStatic(field.getModifiers())) {
                if (!methods.contains("realmGet__${field.name}")) {
                    clazz.addMethod(CtNewMethod.getter("realmGet__${field.name}", field))
                }
                if (!methods.contains("realmSet__${field.name}")) {
                    clazz.addMethod(CtNewMethod.setter("realmSet__${field.name}", field))
                }
            }
        }
    }

    /**
     * Modify a class replacing field accesses with the appropriate Realm accessors
     *
     * @param clazz The CtClass to modify
     * @param managedFields List of fields whose access should be replaced
     */
    public static void useRealmAccessors(CtClass clazz, List<CtField> managedFields) {
        clazz.getDeclaredBehaviors().each { behavior ->
            logger.info "    Behavior: ${behavior.name}"
            if (!behavior.name.startsWith('realmGet__') && !behavior.name.startsWith('realmSet__')) {
                behavior.instrument(new FieldAccessToAccessorConverter(managedFields, clazz, behavior))
            }
        }
    }

    public static void replaceStaticAccessors(CtClass clazz) {
        clazz.getDeclaredBehaviors().each { it.instrument(new StaticToInstanceAccessorConverter()) }
    }

    /**
     * This class goes through all the field access behaviours of a class and replaces field accesses with
     * the appropriate accessor.
     */
    private static class FieldAccessToAccessorConverter extends ExprEditor {
        final List<CtField> managedFields
        final CtClass ctClass
        final CtBehavior behavior

        FieldAccessToAccessorConverter(List<CtField> managedFields, CtClass ctClass, CtBehavior behavior) {
            this.managedFields = managedFields
            this.ctClass = ctClass
            this.behavior = behavior
        }

        @Override
        void edit(FieldAccess fieldAccess) throws CannotCompileException {
            try {
                logger.info "      Field being accessed: ${fieldAccess.className}.${fieldAccess.fieldName}"
                def isRealmFieldAccess = managedFields.find {
                    fieldAccess.className.equals(it.declaringClass.name) && fieldAccess.fieldName.equals(it.name)
                }
                if (isRealmFieldAccess != null) {
                    logger.info "        Realm: Manipulating ${ctClass.simpleName}.${behavior.name}(): ${fieldAccess.fieldName}"
                    logger.info "        Methods: ${ctClass.declaredMethods}"
                    def fieldName = fieldAccess.fieldName
                    if (fieldAccess.isReader()) {
                        fieldAccess.replace('$_ = $0.realmGet__' + fieldName + '();')
                    } else if (fieldAccess.isWriter()) {
                        fieldAccess.replace('$0.realmSet__' + fieldName + '($1);')
                    }
                }
            } catch (NotFoundException ignored) {
            }
        }
    }

    /**
     * This class goes through all the method call behaviors of a proxy class and replaces static accessor with
     * instance ones.
     */
    private static class StaticToInstanceAccessorConverter extends ExprEditor {
        @Override
        void edit(MethodCall methodCall) throws CannotCompileException {
            logger.info "      Method being called: ${methodCall.className}.${methodCall.methodName}"
            if (methodCall.className.equals('io.realm.RealmObject') && Modifier.isStatic(methodCall.method.getModifiers())) {
                logger.info "      Replacing method"
                if (methodCall.methodName.equals('get')) {
                    methodCall.replace('{ $_ = $1.realmGet__$2(); }')
                } else if (methodCall.methodName.equals('set')) {
                    methodCall.replace('{ $1.realmSet__$2($3); }')
                }
            }
        }
    }
}
