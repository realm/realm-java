package io.realm.gradle
import com.android.build.api.transform.*
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import groovy.io.FileType
import io.realm.annotations.Ignore
import javassist.*
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

import static com.android.build.api.transform.QualifiedContent.*

class RealmTransformer extends Transform {

    @Override
    String getName() {
        return "RealmTransformer"
    }

    @Override
    Set<ContentType> getInputTypes() {
        return ImmutableSet.<ContentType>of(DefaultContentType.CLASSES)
    }

    @Override
    Set<Scope> getScopes() {
        return Sets.immutableEnumSet(
                Scope.PROJECT)
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        final ArrayList<File> folders = []
        inputs.each {TransformInput input ->
            println "Directory inputs: ${input.directoryInputs*.file}"
            folders.addAll(input.directoryInputs*.file)
        }

        referencedInputs.each {TransformInput input ->
            folders.addAll(input.directoryInputs*.file)
            folders.addAll(input.jarInputs*.file)
        }


        BiMap<File, String> classFiles = HashBiMap.create()

        folders.each { File folder ->
            folder.eachFileRecurse(FileType.FILES) { File file ->
                if (file.name.endsWith('.class')) {
                    classFiles.put(
                            file,
                            file.canonicalPath
                                    .replace(folder.canonicalPath, '')
                                    .replaceFirst('/', '')
                                    .replace('/', '.')
                                    .replace('.class', ''))
                }
            }
        }

        // Don't use ClassPool.getDefault(). Doing so consecutive builds in the same run (e.g. debug+release)
        // will use a cached object and all the classes will result frozen.
        ClassPool classPool = new ClassPool(null)
        classPool.appendSystemPath()

        folders.each { File folder ->
            classPool.appendClassPath(folder.canonicalPath)
        }

        def proxyClasses = classFiles.findAll {key, value ->  key.name.endsWith('RealmProxy.class') }
        println "Proxy Classes: ${proxyClasses*.value}"

        def modelClasses = proxyClasses.collect { key, value ->
            classPool.getCtClass(classFiles.get(key)).superclass
        }
        println "Model Classes: ${modelClasses*.name}"

        def managedFields = []
        modelClasses.each { managedFields.addAll(it.declaredFields) }
        println "Managed Fields: ${managedFields*.name}"

        modelClasses.each { addRealmAccessors(it) }

        classFiles.each { key, value ->
            println "  Modifying class ${value}"
            def ctClass = classPool.getCtClass(value)
            useRealmAccessors(ctClass, managedFields)
            ctClass.writeFile(outputProvider.getContentLocation(
                    'realm', getInputTypes(), getScopes(), Format.DIRECTORY).canonicalPath)
        }
    }

    private void useRealmAccessors(CtClass clazz, List<CtField> managedFields) {
        clazz.getDeclaredBehaviors().each { behavior ->
            println "    Behavior: ${behavior.name}"
            if (!behavior.name.startsWith('realmGetter$') && !behavior.name.startsWith('realmSetter$')) {
                behavior.instrument(new ExpressionEditor(managedFields, clazz, behavior))
            }
        }
    }

    private static void addRealmAccessors(CtClass clazz) {
        println("  Realm: Adding accessors to ${clazz.simpleName}")
        clazz.declaredFields.each { CtField field ->
            if (field.getAnnotation(Ignore.class) == null) {
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
    }

    private class ExpressionEditor extends ExprEditor {
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
