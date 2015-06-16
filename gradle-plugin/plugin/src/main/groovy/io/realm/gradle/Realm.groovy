package io.realm.gradle
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import javassist.*
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.compile.JavaCompile

class Realm implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Make sure the project is either an Android application or library
        def isAndroidApp = project.plugins.withType(AppPlugin)
        def isAndroidLib = project.plugins.withType(LibraryPlugin)
        if (!isAndroidApp && !isAndroidLib) {
            throw new GradleException("'android' or 'android-library' plugin required.")
        }

        // Configure the project
        project.repositories.add(project.repositories.jcenter())
        project.dependencies.add('compile', 'io.realm:realm-android:0.82.0') // TODO: make version dynamic

        // Find all the variants
        final def variants
        if (isAndroidApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        // Do the bytecode manipulation
        variants.all { variant ->
            variant.dex.doFirst() {// Get the jar files of the imported libraries
                JavaCompile javaCompile = variant.javaCompile
                FileCollection classpathFileCollection = project.files(project.android.bootClasspath)
                classpathFileCollection += javaCompile.classpath

                // Initialize the class container for Javassist
                ClassPool classPool = new ClassPool();

                // Add the classpath jar files
                classpathFileCollection.each {
                    classPool.appendClassPath(it.toString())
                }

                // Append the folder containing the .class files for the project
                classPool.appendClassPath(javaCompile.destinationDir.path)

                // Find the class files
                ConfigurableFileTree classFiles = project.fileTree(javaCompile.destinationDir);
                classFiles.include("**/*.class");


                def classCache = [:]

                // Add custom accessors to the model classes
                classFiles.each { classFile ->
                    CtClass clazz = openClass(classFile, classPool)
                    classCache[classFile] = clazz
                    addRealmAccessors(clazz)
                }

                // Replace access fields with using the custom accessors
                classFiles.each { classFile ->
                    def clazz = classCache[classFile]
                    useRealmAccessors(clazz as CtClass)
                    (clazz as CtClass).writeFile(javaCompile.destinationDir.absolutePath)
                }
            }
        }
    }

    private static void useRealmAccessors(CtClass clazz) {
        clazz.getDeclaredBehaviors().each { behavior ->
            if (!behavior.name.startsWith('realmGetter$') && !behavior.name.startsWith('realmSetter$')) {
                behavior.instrument(new ExprEditor() {
                    @Override
                    void edit(FieldAccess fieldAccess) throws CannotCompileException {
                        if (fieldAccess.field?.declaringClass?.superclass?.name?.equals("io.realm.RealmObject")) {
                            println("Realm: Manipulating ${clazz.simpleName}.${behavior.name}(): ${fieldAccess.fieldName}")
                            def fieldName = fieldAccess.fieldName
                            if (fieldAccess.isReader()) {
                                fieldAccess.replace('$_ = $0.realmGetter$' + fieldName + '();')
                            } else if (fieldAccess.isWriter()) {
                                fieldAccess.replace('$0.realmSetter$' + fieldName + '($1);')
                            }
                        }
                    }
                })
            }
        }
    }

    private static void addRealmAccessors(CtClass clazz) {
        if (clazz.superclass.name.equals("io.realm.RealmObject")) {
            println("Realm: Adding accessors to ${clazz.simpleName}")
            clazz.declaredFields.each { field ->
                // TODO: eventually filter out ignored fields. This would add a dependency on the annotations project
                clazz.addMethod(CtNewMethod.getter("realmGetter\$${field.name}", field))
                clazz.addMethod(CtNewMethod.setter("realmSetter\$${field.name}", field))
            }
        }
    }

    private static CtClass openClass(File classFile, ClassPool classPool) {
        InputStream stream = null
        CtClass clazz = null
        try {
            stream = new DataInputStream(new BufferedInputStream(new FileInputStream(classFile)))
            clazz = classPool.makeClass(stream);
        } catch (Exception ignored) {
            throw new GradleException("Could not load a class for modification")
        } finally {
            if (stream != null) {
                stream.close()
            }
        }
        clazz
    }
}
