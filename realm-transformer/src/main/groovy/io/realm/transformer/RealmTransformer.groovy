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
import io.realm.annotations.RealmClass
import javassist.ClassPool
import javassist.CtClass
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.ClassFile
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.jar.JarFile
import java.util.regex.Pattern

import static com.android.build.api.transform.QualifiedContent.*

/**
 * This class implements the Transform API provided by the Android Gradle plugin.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class RealmTransformer extends Transform {

    private static Logger logger = LoggerFactory.getLogger('realm-logger')
    private Project project

    public RealmTransformer(Project project) {
        this.project = project
    }

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
        // Scope.PROJECT_LOCAL_DEPS and Scope.SUB_PROJECTS_LOCAL_DEPS is only for compatibility with AGP 1.x, 2.x
        return Sets.immutableEnumSet(Scope.EXTERNAL_LIBRARIES, Scope.PROJECT_LOCAL_DEPS,
                Scope.SUB_PROJECTS, Scope.SUB_PROJECTS_LOCAL_DEPS, Scope.TESTED_CODE)
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        def tic = System.currentTimeMillis()

        // Find all the class names available for transforms as well as all referenced classes.

        def outputClassNames = new HashSet<String>()
        def outputReferencedClassNames = new HashSet<String>();
        findClassNames(inputs, outputClassNames, outputReferencedClassNames, isIncremental)

        logger.debug("Incremental build: ${isIncremental.toString()}, files being processed: ${outputClassNames.size()}.")
        if (isIncremental) {
            logger.debug("Incremental files: ${outputClassNames.join(",")}" )
        }
        if (outputClassNames.empty) {
            // Abort transform as quickly as possible if no files where found for processing.
            exitTransform(Collections.emptySet(), Collections.emptyList(), tic)
            return;
        }
        findClassNames(referencedInputs, outputReferencedClassNames, outputReferencedClassNames, isIncremental)
        def allClassNames = merge(outputClassNames, outputReferencedClassNames)

        // Create and populate the Javassist class pool
        ClassPool classPool = new ManagedClassPool(inputs, referencedInputs)
        // Append android.jar to class pool. We don't need the class names of them but only the class in the pool for
        // javassist. See https://github.com/realm/realm-java/issues/2703.
        addBootClassesToClassPool(classPool)

        logger.debug "ClassPool contains Realm classes: ${classPool.getOrNull('io.realm.RealmList') != null}"

        // mark as transformed
        def baseProxyMediator = classPool.get('io.realm.internal.RealmProxyMediator')
        def mediatorPattern = Pattern.compile('^io\\.realm\\.[^.]+Mediator$')
        def proxyMediatorClasses = outputClassNames
                .findAll { it.matches(mediatorPattern) }
                .collect { classPool.getCtClass(it) }
                .findAll { it.superclass?.equals(baseProxyMediator) }

        logger.debug "Proxy Mediator Classes: ${proxyMediatorClasses*.name}"
        proxyMediatorClasses.each {
            BytecodeModifier.overrideTransformedMarker(it)
        }

        // FIXME: Currently doens't handle all cases correctly, e.g. marking an field @Ignore will
        // not transform classes that previously used the non-ignored field.
        CtClass realmObjectProxyInterface = classPool.get("io.realm.internal.RealmObjectProxy");
        def allModelClasses = allClassNames
                // Map strings to CtClass'es.
                .collect { classPool.getCtClass(it) }
                // Model classes either have the @RealmClass annotation directly (if implementing RealmModel)
                // or their superclass has it (if extends RealmObject). The annotation processor
                // will have ensured the annotation is only present in these cases.
                .findAll {
                    if (it.hasAnnotation(RealmClass.class)) {
                        return true
                    }
                    try {
                        return it.superclass?.hasAnnotation(RealmClass.class)
                    } catch (NotFoundException e) {
                        // Can happen if the super class is part of the `android.jar` which might
                        // not have been loaded. In any case, any base class part of Android cannot
                        // be a Realm model class.
                        return false;
                    }
                }
                // Proxy classes are generated by the Realm Annotation Processor and might accidentally
                // parse the above check (e.g. if the model class has the @RealmClass annotation), so
                // ignore them.
                .findAll { !isSubtypeOf(classPool, it, realmObjectProxyInterface) }
                // Unfortunately the RealmObject base class parses all above checks, so explicitly
                // ignore it.
                .findAll { !it.name.equals("io.realm.RealmObject") }

        def outputModelClasses = allModelClasses.findAll {
            outputClassNames.contains(it.name)
        }
        logger.debug "All Model Classes: ${allModelClasses*.name}"

        // Add accessors to the model classes in the target project
        outputModelClasses.each {
            logger.debug("Modify model class: ${it.name}")
            BytecodeModifier.addRealmAccessors(it)
            BytecodeModifier.addRealmProxyInterface(it, classPool)
            BytecodeModifier.callInjectObjectContextFromConstructors(it)
        }

        // Populate a list of the fields that need to be managed with bytecode manipulation
        def allManagedFields = []
        allModelClasses.each {
            allManagedFields.addAll(it.declaredFields.findAll {
                BytecodeModifier.isModelField(it)
            })
        }
        logger.debug "Managed Fields: ${allManagedFields*.name}"


        // Use accessors instead of direct field access
        outputClassNames.each {
            logger.debug "Modify accessors in class: ${it}"
            def ctClass = classPool.getCtClass(it)
            BytecodeModifier.useRealmAccessors(ctClass, allManagedFields)
            ctClass.writeFile(getOutputFile(outputProvider).canonicalPath)
        }

        copyResourceFiles(inputs, outputProvider)
        classPool.close()
        exitTransform(inputs, outputModelClasses, tic)
    }

    /**
     * Returns {@code true} if 'clazz' is considered a subtype of 'superType'.
     *
     * This function is different than {@link CtClass#subtypeOf(CtClass)} in the sense
     * that it will never crash even if classes are missing from the class pool, instead
     * it will just return {@code false}.
     *
     * This e.g. happens with RxJava classes which are optional, but JavaAssist will try
     * to load them and then crash.
     *
     * @param classPool pool of all known classes
     * @param clazz class to check
     * @param typeToCheckAgainst the type we want to check against
     * @return
     */
    private boolean isSubtypeOf(ClassPool classPool, CtClass clazz, CtClass typeToCheckAgainst) {
        String typeToCheckAgainstQualifiedName = typeToCheckAgainst.getName();
        if (clazz == typeToCheckAgainst || clazz.getName().equals(typeToCheckAgainstQualifiedName))
            return true;

        ClassFile file = clazz.getClassFile2();

        // Check direct super class
        String superName = file.getSuperclass();
        if (superName != null && superName.equals(typeToCheckAgainstQualifiedName))
            return true;

        // Check direct interfaces
        String[] ifs = file.getInterfaces();
        int num = ifs.length;
        for (int i = 0; i < num; i++) {
            if (ifs[i].equals(typeToCheckAgainstQualifiedName)) {
                return true;
            }
        }

        // Check other inherited super classes
        if (superName != null) {
            CtClass nextSuper
            try {
                nextSuper = classPool.get(superName)
                if (isSubtypeOf(classPool, nextSuper, typeToCheckAgainst)) {
                    return true;
                }
            } catch (NotFoundException ignored) {
            }
        }

        // Check other inherited interfaces
        for (int i = 0; i < num; i++) {
            try {
                CtClass interfaceClass = classPool.get(ifs[i])
                if (isSubtypeOf(classPool, interfaceClass, typeToCheckAgainst)) {
                    return true;
                }
            } catch (NotFoundException ignored) {
            }
        }

        return false;
    }

    // Called when the transformer is exiting
    private exitTransform(Collection<TransformInput> inputs, List<CtClass> outputModelClasses, long startTime) {
        def endTime = System.currentTimeMillis()
        logger.debug "Realm Transform time: ${endTime-startTime} milliseconds"
        this.sendAnalytics(inputs, outputModelClasses)
    }

    /**
     * Sends the analytics
     * @param inputs the inputs provided by the Transform API
     * @param inputModelClasses a list of ctClasses describing the Realm models
     */
    private sendAnalytics(Collection<TransformInput> inputs, List<CtClass> outputModelClasses) {
        def disableAnalytics = "true".equals(System.getenv()["REALM_DISABLE_ANALYTICS"])
        if (inputs.empty || disableAnalytics) {
            // Don't send analytics for incremental builds or if they have ben explicitly disabled.
            return
        }

        def containsKotlin = false

        outer:
        for(TransformInput input : inputs) {
            for (DirectoryInput di : input.directoryInputs) {
                def path = di.file.absolutePath
                def index = path.indexOf('build' + File.separator + 'intermediates' + File.separator + 'classes')
                if (index != -1) {
                    def projectPath = path.substring(0, index)
                    def buildFile = new File(projectPath + 'build.gradle')
                    if (buildFile.exists() && buildFile.text.contains('kotlin')) {
                        containsKotlin = true
                        break outer
                    }
                }
            }
        }

        def packages = outputModelClasses.collect {
            it.getPackageName()
        }

        def targetSdk = project?.android?.defaultConfig?.targetSdkVersion?.mApiLevel as String
        def minSdk = project?.android?.defaultConfig?.minSdkVersion?.mApiLevel as String

        if (disableAnalytics) {
            boolean sync = project?.realm?.syncEnabled != null && project.realm.syncEnabled
            def analytics = new RealmAnalytics(packages as Set, containsKotlin, sync, targetSdk, minSdk)
            analytics.execute()
        }
    }

    /**
     * Go through the transform input in order to find all files we need to transform.
     *
     * @param inputs set of input files
     * @param directoryFiles the set of files in directories getting compiled. These are candidates for the transformer.
     * @param referencedFiles the set of files that are possible referenced but never transformed (required by JavaAssist).
     * @param isIncremental {@code true} if build is incremental.
     */
    private static void findClassNames(Collection<TransformInput> inputs, Set<String> directoryFiles, Set<String> referencedFiles, isIncremental) {
        inputs.each {

            // Files in directories are files we most likely want to transform, unless they are
            // marked as referenced scope. See {@link Transform#getReferencedScopes()}.
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                if (isIncremental) {
                    // Incremental builds: Include all changed or added files, i.e. ignore deleted files
                    it.changedFiles.entrySet().each {
                        if (it.value == Status.NOTCHANGED || it.value == Status.REMOVED) {
                            return
                        }
                        def filePath = it.key.absolutePath
                        if (filePath.endsWith(SdkConstants.DOT_CLASS)) {
                            def className =
                                    filePath.substring(
                                            dirPath.length() + 1,
                                            filePath.length() - SdkConstants.DOT_CLASS.length()
                                    ).replace(File.separatorChar, '.' as char)
                            directoryFiles.add(className)
                        }
                    }

                } else {
                    // Non-incremental build: Include all files
                    it.file.eachFileRecurse(FileType.FILES) {
                        if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                            def className =
                                    it.absolutePath.substring(
                                            dirPath.length() + 1,
                                            it.absolutePath.length() - SdkConstants.DOT_CLASS.length()
                                    ).replace(File.separatorChar, '.' as char)
                            directoryFiles.add(className)
                        }
                    }
                }
            }

            // Files in Jars are always treated as referenced input. They should already have been
            // modified by the transformer in the project that built the jar.
            it.jarInputs.each {
                if (isIncremental && (it.status == Status.REMOVED)) {
                    return;
                }

                def jarFile = new JarFile(it.file)
                jarFile.entries().findAll {
                    !it.directory && it.name.endsWith(SdkConstants.DOT_CLASS)
                }.each {
                    def path = it.name
                    // The jar might not using File.separatorChar as the path separator. So we just replace both `\` and
                    // `/`. It depends on how the jar file was created.
                    // See http://stackoverflow.com/questions/13846000/file-separators-of-path-name-of-zipentry
                    String className = path.substring(0, path.length() - SdkConstants.DOT_CLASS.length())
                            .replace('/' as char , '.' as char)
                            .replace('\\' as char , '.' as char)
                    referencedFiles.add(className)
                }
                jarFile.close() // Crash transformer if this fails
            }
        }
    }

    private copyResourceFiles(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                it.file.eachFileRecurse(FileType.FILES) {
                    if (!it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        logger.debug "  Copying resource ${it}"
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
        return merged
    }

    // There is no official way to get the path to android.jar for transform.
    // See https://code.google.com/p/android/issues/detail?id=209426
    private void addBootClassesToClassPool(ClassPool classPool) {
        try {
            project.android.bootClasspath.each {
                String path = it.absolutePath
                logger.debug "Add boot class " + path + " to class pool."
                classPool.appendClassPath(path)
            }
        } catch (Exception e) {
            // Just log it. It might not impact the transforming if the method which needs to be transformer doesn't
            // contain classes from android.jar.
            logger.debug("Cannot get bootClasspath caused by:", e)
        }
    }
}
