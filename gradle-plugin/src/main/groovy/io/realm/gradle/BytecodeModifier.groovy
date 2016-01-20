package io.realm.gradle

import javassist.CannotCompileException
import javassist.CtBehavior
import javassist.CtClass
import javassist.CtField
import javassist.CtNewMethod
import javassist.NotFoundException
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

class BytecodeModifier {
    public static void addRealmAccessors(CtClass clazz) {
        println("  Realm: Adding accessors to ${clazz.simpleName}")
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
        clazz.getDeclaredBehaviors().each { behavior ->
            println "    Behavior: ${behavior.name}"
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
            try {
                println "      Field being accessed: ${fieldAccess.className}.${fieldAccess.fieldName}"
                def flag = false
                managedFields.each {
                    if (fieldAccess.className.equals(it.declaringClass.name) && fieldAccess.fieldName.equals(it.name)) {
                        flag = true
                    }
                }
                if (flag) {
                    println("        Realm: Manipulating ${ctClass.simpleName}.${behavior.name}(): ${fieldAccess.fieldName}")
                    println("        Methods: ${ctClass.declaredMethods}")
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
