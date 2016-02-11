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
import com.android.SdkConstants
import com.android.build.api.transform.*
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import groovy.io.FileType
import io.realm.annotations.Ignore
import javassist.ClassPool
import javassist.LoaderClassPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Modifier
import java.util.jar.JarFile

import static com.android.build.api.transform.QualifiedContent.*
/**
 * This class implements the Transform API provided by the Android Gradle plugin.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
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
        return Sets.immutableEnumSet(Scope.PROJECT)
    }

    @Override
    Set<Scope> getReferencedScopes() {
        return Sets.immutableEnumSet(Scope.EXTERNAL_LIBRARIES, Scope.TESTED_CODE)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        def tic = System.currentTimeMillis()

        // Find all the class names
        def classNames = getClassNames(inputs)

        // Create and populate the Javassist class pool
        ClassPool classPool = createClassPool(inputs, referencedInputs)

        logger.info "ClassPool contains Realm classes: ${classPool.getOrNull('io.realm.RealmList') != null}"

        // Find the model classes
        def realmObject = classPool.get('io.realm.RealmObject')
        def modelClasses = classNames
                .findAll { it.endsWith('RealmProxy') }
                .collect { classPool.getCtClass(it).superclass }
                .findAll { it.superclass?.equals(realmObject) }
        logger.info "Model Classes: ${modelClasses*.name}"

        // Populate a list of the fields that need to be managed with bytecode manipulation
        def managedFields = []
        modelClasses.each {
            managedFields.addAll(it.declaredFields.findAll {
                it.getAnnotations()
                it.getAnnotation(Ignore.class) == null && !Modifier.isStatic(it.getModifiers())
            })
        }
        logger.info "Managed Fields: ${managedFields*.name}"

        // Add accessors to the model classes
        modelClasses.each {
            BytecodeModifier.addRealmAccessors(it)
            BytecodeModifier.addRealmProxyInterface(it, classPool)
        }

        // Use accessors instead of direct field access
        classNames.each {
            logger.info "  Modifying class ${it}"
            def ctClass = classPool.getCtClass(it)
            BytecodeModifier.useRealmAccessors(ctClass, managedFields, modelClasses)
            ctClass.writeFile(outputProvider.getContentLocation(
                    'realm', getInputTypes(), getScopes(), Format.DIRECTORY).canonicalPath)
        }

        def toc = System.currentTimeMillis()
        logger.info "Realm Transform time: ${toc-tic} milliseconds"
    }

    /**
     * Create and populate the Javassist class pool.
     *
     * @param inputs The inputs provided by the Transform API
     * @param referencedInputs the referencedInputs provided by the Transform API
     * @return the populated ClassPool instance
     */
    private ClassPool createClassPool(Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs) {
        // Don't use ClassPool.getDefault(). Doing consecutive builds in the same run (e.g. debug+release)
        // will use a cached object and all the classes will be frozen.
        ClassPool classPool = new ClassPool(null)
        classPool.appendSystemPath()
        classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()))

        inputs.each {
            it.directoryInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }

            it.jarInputs.stream().each {
                classPool.appendClassPath(it.file.absolutePath)
            }
        }

        referencedInputs.each {
            it.directoryInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }

            it.jarInputs.stream().each {
                classPool.appendClassPath(it.file.absolutePath)
            }
        }

        return classPool
    }

    private static Set<String> getClassNames(Collection<TransformInput> inputs) {
        Set<String> classNames = new HashSet<String>()

        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                it.file.eachFileRecurse(FileType.FILES) {
                    if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        def className =
                                it.absolutePath.substring(
                                        dirPath.length() + 1,
                                        it.absolutePath.length() - SdkConstants.DOT_CLASS.length()
                                ).replace(File.separatorChar, '.' as char)
                        classNames.add(className)
                    }
                }
            }

            it.jarInputs.stream().each {
                def jarFile = new JarFile(it.file)
                jarFile.stream()
                        .filter { it.name.endsWith(SdkConstants.DOT_CLASS) }
                        .each { classNames.add(it.name.substring(0, it.name.length() - SdkConstants.DOT_CLASS.length()).replaceAll('/', '.')) }
            }
        }

        return classNames
    }

}
