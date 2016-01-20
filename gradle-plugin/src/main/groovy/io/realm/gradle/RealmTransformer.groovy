package io.realm.gradle
import com.android.build.api.transform.*
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import groovy.io.FileType
import io.realm.annotations.Ignore
import javassist.ClassPool
import javassist.LoaderClassPath

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
        classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()))

        folders.each { File folder ->
            classPool.appendClassPath(folder.canonicalPath)
        }

        println "Contains io.realm.RealmList: ${classPool.getOrNull('io.realm.RealmList')}"

        def proxyClasses = classFiles.findAll {key, value ->  key.name.endsWith('RealmProxy.class') }
        println "Proxy Classes: ${proxyClasses*.value}"

        def modelClasses = proxyClasses.collect { key, value ->
            classPool.getCtClass(classFiles.get(key)).superclass
        }
        println "Model Classes: ${modelClasses*.name}"

        def managedFields = []
        modelClasses.each {
            managedFields.addAll(it.declaredFields.findAll { it.getAnnotation(Ignore.class) == null })
        }
        println "Managed Fields: ${managedFields*.name}"

        modelClasses.each { BytecodeModifier.addRealmAccessors(it) }

        classFiles.each { key, value ->
            println "  Modifying class ${value}"
            def ctClass = classPool.getCtClass(value)
            BytecodeModifier.useRealmAccessors(ctClass, managedFields)
            ctClass.writeFile(outputProvider.getContentLocation(
                    'realm', getInputTypes(), getScopes(), Format.DIRECTORY).canonicalPath)
        }
    }

}
