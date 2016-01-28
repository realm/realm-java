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

import com.android.build.api.transform.*
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import groovy.io.FileType
import io.realm.annotations.Ignore
import javassist.ClassPool
import javassist.LoaderClassPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.android.build.api.transform.QualifiedContent.*

class RealmTransformer extends Transform {

    private Logger logger = LoggerFactory.getLogger('realm-logger')

    @Override
    String getName() {
        return "RealmTransformer"
    }

    @Override
    Set<ContentType> getInputTypes() {
        return ImmutableSet.<ContentType> of(DefaultContentType.CLASSES)
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
        inputs.each { TransformInput input ->
            logger.info "Directory inputs: ${input.directoryInputs*.file}"
            folders.addAll(input.directoryInputs*.file)
        }

        referencedInputs.each { TransformInput input ->
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

        logger.info "Contains io.realm.RealmList: ${classPool.getOrNull('io.realm.RealmList')}"

        def proxyClasses = classFiles.findAll { key, value -> key.name.endsWith('RealmProxy.class') }
        logger.info "Proxy Classes: ${proxyClasses*.value}"

        def modelClasses = proxyClasses.collect { key, value ->
            classPool.getCtClass(classFiles.get(key)).superclass
        }
        logger.info "Model Classes: ${modelClasses*.name}"

        def managedFields = []
        modelClasses.each {
            managedFields.addAll(it.declaredFields.findAll {
                it.getAnnotation(Ignore.class) == null
            })
        }
        logger.info "Managed Fields: ${managedFields*.name}"

        modelClasses.each { BytecodeModifier.addRealmAccessors(it) }

        classFiles.each { key, value ->
            logger.info "  Modifying class ${value}"
            def ctClass = classPool.getCtClass(value)
            BytecodeModifier.useRealmAccessors(ctClass, managedFields)
            ctClass.writeFile(outputProvider.getContentLocation(
                    'realm', getInputTypes(), getScopes(), Format.DIRECTORY).canonicalPath)
        }
    }

}
