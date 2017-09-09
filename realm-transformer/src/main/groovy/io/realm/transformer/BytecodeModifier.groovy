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
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * This class encapsulates the bytecode manipulation code needed to transform model classes
 * and the classes using them.
 */
class BytecodeModifier {

    private static final Logger logger = LoggerFactory.getLogger('realm-logger')

    static boolean isModelField(CtField field) {
        return !field.hasAnnotation(Ignore.class) && !Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())
    }

    /**
     * Adds Realm specific accessors to a model class.
     * All the declared fields will be associated with a getter and a setter.
     *
     * @param clazz the CtClass to add accessors to.
     */
    public static void addRealmAccessors(CtClass clazz) {
        logger.debug "  Realm: Adding accessors to ${clazz.simpleName}"
        def methods = clazz.getDeclaredMethods()*.name
        clazz.declaredFields.each { CtField field ->
            if (isModelField(field)) {
                if (!methods.contains("realmGet\$${field.name}".toString())) {
                    clazz.addMethod(CtNewMethod.getter("realmGet\$${field.name}", field))
                }
                if (!methods.contains("realmSet\$${field.name}".toString())) {
                    clazz.addMethod(CtNewMethod.setter("realmSet\$${field.name}", field))
                }
            }
        }
    }

    /**
     * Modifies a class replacing field accesses with the appropriate Realm accessors.
     *
     * @param clazz The CtClass to modify
     * @param managedFields List of fields whose access should be replaced
     */
    public static void useRealmAccessors(CtClass clazz, List<CtField> managedFields) {
        clazz.getDeclaredBehaviors().each { behavior ->
            logger.debug "    Behavior: ${behavior.name}"
            if (
                (
                    behavior instanceof CtMethod &&
                    !behavior.name.startsWith('realmGet$') &&
                    !behavior.name.startsWith('realmSet$')
                ) || (
                    behavior instanceof CtConstructor
                )
            ) {
                behavior.instrument(new FieldAccessToAccessorConverter(managedFields, clazz, behavior))
            }
        }
    }

    /**
     * Modifies a class adding its RealmProxy interface.
     *
     * @param clazz The CtClass to modify
     * @param classPool the Javassist class pool
     */
    public static void addRealmProxyInterface(CtClass clazz, ClassPool classPool) {
        def proxyInterface = classPool.get("io.realm.${clazz.getSimpleName()}RealmProxyInterface")
        clazz.addInterface(proxyInterface)
    }

    public static void callInjectObjectContextFromConstructors(CtClass clazz) {
        clazz.getConstructors().each {
            it.insertBeforeBody('if ($0 instanceof io.realm.internal.RealmObjectProxy) {' +
                    ' ((io.realm.internal.RealmObjectProxy) $0).realm$injectObjectContext();' +
                    ' }')
        }
    }

    /**
     * This class goes through all the field access behaviours of a class and replaces field accesses with
     * the appropriate accessor.
     */
    private static class FieldAccessToAccessorConverter extends ExprEditor {
        final List<CtField> managedFields
        final CtClass ctClass
        final CtBehavior behavior

        FieldAccessToAccessorConverter(List<CtField> managedFields,
                                       CtClass ctClass,
                                       CtBehavior behavior) {
            this.managedFields = managedFields
            this.ctClass = ctClass
            this.behavior = behavior
        }

        @Override
        void edit(FieldAccess fieldAccess) throws CannotCompileException {
            logger.debug "      Field being accessed: ${fieldAccess.className}.${fieldAccess.fieldName}"
            def isRealmFieldAccess = managedFields.find {
                fieldAccess.className.equals(it.declaringClass.name) && fieldAccess.fieldName.equals(it.name)
            }
            if (isRealmFieldAccess != null) {
                logger.debug "        Realm: Manipulating ${ctClass.simpleName}.${behavior.name}(): ${fieldAccess.fieldName}"
                logger.debug "        Methods: ${ctClass.declaredMethods}"
                def fieldName = fieldAccess.fieldName
                if (fieldAccess.isReader()) {
                    fieldAccess.replace('$_ = $0.realmGet$' + fieldName + '();')
                } else if (fieldAccess.isWriter()) {
                    fieldAccess.replace('$0.realmSet$' + fieldName + '($1);')
                }
            }
        }
    }

    /**
     * Adds a method to indicate that Realm transformer has been applied.
     *
     * @param clazz The CtClass to modify.
     */
    public static void overrideTransformedMarker(CtClass clazz) {
        logger.debug "  Realm: Marking as transformed ${clazz.simpleName}"
        try {
            clazz.getDeclaredMethod("transformerApplied", new CtClass[0])
        } catch (NotFoundException ignored) {
            clazz.addMethod(CtNewMethod.make(Modifier.PUBLIC, CtClass.booleanType, "transformerApplied",
                    new CtClass[0], new CtClass[0], "{return true;}", clazz))
        }
    }
}
