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
import com.google.common.io.Files
import groovy.io.FileType
import io.realm.annotations.Ignore
import javassist.ClassPool
import javassist.LoaderClassPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Modifier
import java.util.jar.JarFile
import java.util.regex.Pattern

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
        return Sets.immutableEnumSet(Scope.EXTERNAL_LIBRARIES, Scope.PROJECT_LOCAL_DEPS,
                Scope.SUB_PROJECTS, Scope.SUB_PROJECTS_LOCAL_DEPS, Scope.TESTED_CODE)
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
        def inputClassNames = getClassNames(inputs)
        def referencedClassNames = getClassNames(referencedInputs)
        def allClassNames = merge(inputClassNames, referencedClassNames);

        // Create and populate the Javassist class pool
        ClassPool classPool = createClassPool(inputs, referencedInputs)

        logger.info "ClassPool contains Realm classes: ${classPool.getOrNull('io.realm.RealmList') != null}"

        // mark as transformed
        def baseProxyMediator = classPool.get('io.realm.internal.RealmProxyMediator')
        def mediatorPattern = Pattern.compile('^io\\.realm\\.[^.]+Mediator$')
        def proxyMediatorClasses = inputClassNames
                .findAll { it.matches(mediatorPattern) }
                .collect { classPool.getCtClass(it) }
                .findAll { it.superclass?.equals(baseProxyMediator) }
        logger.info "Proxy Mediator Classes: ${proxyMediatorClasses*.name}"
        proxyMediatorClasses.each {
            BytecodeModifier.overrideTransformedMarker(it);
        }

        // Find the model classes
        def realmObject = classPool.get('io.realm.RealmObject')
        def allModelClasses = allClassNames
                .findAll { it.endsWith('RealmProxy') }
                .collect { classPool.getCtClass(it).superclass }
                .findAll { it.superclass?.equals(realmObject) }
        def inputModelClasses = allModelClasses.findAll {
            inputClassNames.contains(it.name)
        }
        logger.info "Model Classes: ${allModelClasses*.name}"

        // Populate a list of the fields that need to be managed with bytecode manipulation
        def allManagedFields = []
        allModelClasses.each {
            allManagedFields.addAll(it.declaredFields.findAll {
                !it.hasAnnotation(Ignore.class) && !Modifier.isStatic(it.getModifiers())
            })
        }
        logger.info "Managed Fields: ${allManagedFields*.name}"

        // Add accessors to the model classes in the target project
        inputModelClasses.each {
            BytecodeModifier.addRealmAccessors(it)
            BytecodeModifier.addRealmProxyInterface(it, classPool)
        }

        // Use accessors instead of direct field access
        inputClassNames.each {
            logger.info "  Modifying class ${it}"
            def ctClass = classPool.getCtClass(it)
            BytecodeModifier.useRealmAccessors(ctClass, allManagedFields, allModelClasses)
            ctClass.writeFile(getOutputFile(outputProvider).canonicalPath)
        }

        copyResourceFiles(inputs, outputProvider)

        def toc = System.currentTimeMillis()
        logger.info "Realm Transform time: ${toc-tic} milliseconds"
    }

    /**
     * Creates and populates the Javassist class pool.
     *
     * @param inputs the inputs provided by the Transform API
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

            it.jarInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }
        }

        referencedInputs.each {
            it.directoryInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }

            it.jarInputs.each {
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

            it.jarInputs.each {
                def jarFile = new JarFile(it.file)
                jarFile.entries().findAll {
                    !it.directory && it.name.endsWith(SdkConstants.DOT_CLASS)
                }.each {
                    def path = it.name
                    // The jar might not using File.separatorChar as the path separator. So we just replace both `\` and
                    // `/`. It depends on how the jar file was created.
                    // See http://stackoverflow.com/questions/13846000/file-separators-of-path-name-of-zipentry
                    def className = path.substring(0, path.length() - SdkConstants.DOT_CLASS.length())
                            .replace('/' as char , '.' as char)
                            .replace('\\' as char , '.' as char)
                    classNames.add(className)
                }
            }
        }
        return classNames
    }

    private copyResourceFiles(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                it.file.eachFileRecurse(FileType.FILES) {
                    if (!it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        logger.info "  Copying resource ${it}"
                        def dest = new File(getOutputFile(outputProvider),
                                it.absolutePath.substring(dirPath.length()))
                        dest.parentFile.mkdirs()
                        Files.copy(it, dest)
                    }
                }
            }

            // no need to implement the code for `it.jarInputs.each` since PROJECT SCOPE does not use jar input.
        }
    }

    private File getOutputFile(TransformOutputProvider outputProvider) {
        return outputProvider.getContentLocation(
                'realm', getInputTypes(), getScopes(), Format.DIRECTORY)
    }

    private static Set<String> merge(Set<String> set1, Set<String> set2) {
        Set<String> merged = new HashSet<String>()
        merged.addAll(set1)
        merged.addAll(set2)
        return merged;
    }
}
