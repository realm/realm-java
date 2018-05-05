package io.realm.transformer

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.google.common.io.Files
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.NotFoundException
import javassist.bytecode.ClassFile
import org.gradle.api.Project
import java.io.File
import java.util.regex.Pattern

/**
 * Abstract defining the structure of transforming both a full and incremental build.
 */
abstract class TransformBuildTemplate(val project: Project, val outputProvider: TransformOutputProvider, val transform: Transform) {

    protected lateinit var inputs: MutableCollection<TransformInput>
    protected lateinit var classPool: ManagedClassPool
    protected val outputClassNames: MutableSet<String> = hashSetOf()
    protected val outputReferencedClassNames: MutableSet<String> = hashSetOf()
    protected val outputModelClasses: ArrayList<CtClass> = arrayListOf()
    protected val allModelClasses: ArrayList<CtClass> = arrayListOf()

    /**
     * Find all the class names available for transforms as well as all referenced classes.
     */
    abstract fun prepareOutputClasses(inputs: MutableCollection<TransformInput>)

    /**
     * Helper method for going through all `TransformInput` and sort classes into either
     * the set of files that should be transformed or the set of files that are just references.
     *
     * @param inputs set of input files
     * @param directoryFiles the set of files in directories getting compiled. These are candidates for the transformer.
     * @param referencedFiles the set of files that are possible referenced but never transformed (required by JavaAssist).
     */
    protected abstract fun findClassNames(inputs: Collection<TransformInput>,
                                          directoryFiles: MutableSet<String>,
                                          referencedFiles: MutableSet<String>)

    /**
     * Returns `true` if this build contains no relevant classes to transform.
     */
    fun hasNoOutput(): Boolean {
        return outputClassNames.isEmpty()
    }

    fun prepareReferencedClasses(referencedInputs: Collection<TransformInput>) {
        findClassNames(referencedInputs, outputReferencedClassNames, outputReferencedClassNames) // referenced files
        val allClassNames: Set<String> = merge(outputClassNames, outputReferencedClassNames)

        // Create and populate the Javassist class pool
        this.classPool = ManagedClassPool(inputs, referencedInputs)
        // Append android.jar to class pool. We don't need the class names of them but only the class in the pool for
        // javassist. See https://github.com/realm/realm-java/issues/2703.
        addBootClassesToClassPool(classPool)
        logger.debug("ClassPool contains Realm classes: ${classPool.getOrNull("io.realm.RealmList") != null}")


        // Different impl
        allModelClasses.addAll(findModelClasses(allClassNames))
        logger.debug("All Model Classes: ${allModelClasses.joinToString(",") { it.name }}")

        outputModelClasses.addAll(allModelClasses.filter {
            outputClassNames.contains(it.name)
        })
    }

    fun markMediatorsAsTransformed() {
        val baseProxyMediator: CtClass = classPool.get("io.realm.internal.RealmProxyMediator")
        val mediatorPattern: Pattern = Pattern.compile("^io\\.realm\\.[^.]+Mediator$")
        val proxyMediatorClasses: Collection<CtClass> = outputClassNames
                .filter { mediatorPattern.matcher(it).find() }
                .map { classPool.getCtClass(it) }
                .filter { it.superclass.equals(baseProxyMediator) }

        logger.debug("Proxy Mediator Classes: ${proxyMediatorClasses.joinToString(",") { it.name }}")
        proxyMediatorClasses.forEach {
            BytecodeModifier.overrideTransformedMarker(it)
        }
    }

    fun transformModelClasses() {
        // Add accessors to the model classes in the target project
        outputModelClasses.forEach {
            logger.debug("Modify model class: ${it.name}")
            BytecodeModifier.addRealmAccessors(it)
            BytecodeModifier.addRealmProxyInterface(it, classPool)
            BytecodeModifier.callInjectObjectContextFromConstructors(it)
        }
    }

    fun transformDirectAccessToModelFields() {
        // Populate a list of the fields that need to be managed with bytecode manipulation
        val allManagedFields: ArrayList<CtField> = arrayListOf()
        allModelClasses.forEach {
            allManagedFields.addAll(it.declaredFields.filter {
                BytecodeModifier.isModelField(it)
            })
        }
        logger.debug("Managed Fields: ${allManagedFields.joinToString(",") { it.name }}")

        // Use accessors instead of direct field access
        outputClassNames.forEach {
            logger.debug("Modify accessors in class: $it")
            val ctClass: CtClass = classPool.getCtClass(it)
            BytecodeModifier.useRealmAccessors(ctClass, allManagedFields)
            ctClass.writeFile(getOutputFile(outputProvider).canonicalPath)
        }
    }

    fun copyResourceFiles() {
        copyResourceFiles(inputs)
        classPool.close();
    }

    private fun copyResourceFiles(inputs: MutableCollection<TransformInput>) {
        inputs.forEach {
            it.directoryInputs.forEach {
                val dirPath: String = it.file.absolutePath
                it.file.walkTopDown().forEach {
                    if (it.isFile) {
                        if (!it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                            logger.debug("  Copying resource $it")
                            val dest = File(getOutputFile(outputProvider), it.absolutePath.substring(dirPath.length))
                            dest.parentFile.mkdirs()
                            Files.copy(it, dest)
                        }
                    }
                }
            }
            // no need to implement the code for `it.jarInputs.each` since PROJECT SCOPE does not use jar input.
        }
    }

    private fun getOutputFile(outputProvider: TransformOutputProvider): File {
        return outputProvider.getContentLocation(
                "realm", transform.inputTypes, transform.scopes, Format.DIRECTORY)
    }

    /**
     * There is no official way to get the path to android.jar for transform.
     * See https://code.google.com/p/android/issues/detail?id=209426
     */
    private fun addBootClassesToClassPool(classPool: ClassPool) {
        try {
            GroovyUtil.getBootClasspath(project).forEach {
                val path: String = it.absolutePath
                logger.debug("Add boot class $path to class pool.")
                classPool.appendClassPath(path)
            }
        } catch (e: Exception) {
            // Just log it. It might not impact the transforming if the method which needs to be transformer doesn't
            // contain classes from android.jar.
            logger.debug("Cannot get bootClasspath caused by: ", e)
        }
    }

    private fun merge(set1: Set<String>, set2: Set<String>): Set<String>  {
        val merged: MutableSet<String> = hashSetOf()
        merged.addAll(set1)
        merged.addAll(set2)
        return merged
    }

    fun getOutputModelClasses(): Collection<CtClass> {
        return outputModelClasses
    }

    protected abstract fun findModelClasses(classNames: Set<String>): Collection<CtClass>

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
     * @return `true` if `clazz` is a subtype of `typeToCheckAgainst`, `false` otherwise.
     */
    protected fun isSubtypeOf(clazz: CtClass, typeToCheckAgainst: CtClass): Boolean {
        val typeToCheckAgainstQualifiedName: String = typeToCheckAgainst.name
        if (clazz == typeToCheckAgainst || clazz.name.equals(typeToCheckAgainstQualifiedName)) {
            return true
        }

        val file: ClassFile = clazz.getClassFile2();

        // Check direct super class
        val superName: String? = file.getSuperclass();
        if (superName.equals(typeToCheckAgainstQualifiedName)) {
            return true;
        }

        // Check direct interfaces
        val ifs: Array<String> = file.interfaces;
        val num: Int = ifs.size
        for (i in 0..num) {
            if (ifs[i] == typeToCheckAgainstQualifiedName) {
                return true;
            }
        }

        // Check other inherited super classes
        if (superName != null) {
            var nextSuper: CtClass
            try {
                nextSuper = classPool.get(superName)
                if (isSubtypeOf(nextSuper, typeToCheckAgainst)) {
                    return true
                }
            } catch (ignored: NotFoundException) {
            }
        }

        // Check other inherited interfaces
        for (i in 0..num) {
            try {
                val interfaceClass: CtClass = classPool.get(ifs[i])
                if (isSubtypeOf(interfaceClass, typeToCheckAgainst)) {
                    return true;
                }
            } catch (ignored: NotFoundException) {
            }
        }

        return false;
    }

}
