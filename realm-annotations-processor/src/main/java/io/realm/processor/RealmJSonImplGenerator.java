package io.realm.processor;

import com.squareup.javawriter.JavaWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

/**
 * This class generates the RealmJsonImpl class which is responsible for importing json data into either standalone
 * objects or RealmObjects.
 */
public class RealmJSonImplGenerator {

    private final ProcessingEnvironment processingEnvironment;
    private List<String> qualifiedModelClasses = new ArrayList<String>();
    private List<String> simpleModelClasses = new ArrayList<String>();
    private List<String> proxyClasses = new ArrayList<String>();

    private static final String REALM_PACKAGE_NAME = "io.realm";
    private static final String CLASS_NAME = "RealmJsonImpl";
    private static final String EXCEPTION_MSG = "\"Could not find the generated proxy class for \" + classQualifiedName";

    public RealmJSonImplGenerator(ProcessingEnvironment processingEnv, Set<ClassMetaData> classesToValidate) {
        this.processingEnvironment = processingEnv;
        for (ClassMetaData metadata: classesToValidate) {
            qualifiedModelClasses.add(metadata.getFullyQualifiedClassName());
            simpleModelClasses.add(metadata.getSimpleClassName());
            proxyClasses.add(Utils.getProxyClassName(metadata.getSimpleClassName()));
        }
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%s", REALM_PACKAGE_NAME, CLASS_NAME);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(REALM_PACKAGE_NAME);
        writer.emitEmptyLine();

        writer.emitImports(
                "android.util.JsonReader",
                "io.realm.exceptions.RealmException",
                "io.realm.internal.RealmJson",
                "java.io.IOException",
                "java.util.ArrayList",
                "java.util.Collections",
                "java.util.List",
                "org.json.JSONException",
                "org.json.JSONObject"
        );
        writer.emitImports(qualifiedModelClasses);
        writer.emitEmptyLine();
        writer.beginType(
                qualifiedGeneratedClassName,    // full qualified name of the item to generate
                "class",                        // the type of the item
                Collections.<Modifier>emptySet(),      // modifiers to apply
                null,                           // class to extend
                "RealmJson");                   // Interfaces to implement
        writer.emitEmptyLine();

        emitCreateOrUpdateUsingJsonObject(writer);
        emitCreateUsingJsonStream(writer);

        writer.endType();
        writer.close();
    }

    private void emitCreateOrUpdateUsingJsonObject(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmObject> E",
                "createOrUpdateUsingJsonObject",
                EnumSet.of(Modifier.PUBLIC),
                Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JSONObject", "json", "boolean", "update"),
                Arrays.asList("JSONException")
        );
        emitProxySwitch("return (E) %s.createOrUpdateUsingJsonObject(realm, json, update)", writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCreateUsingJsonStream(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmObject> E",
                "createUsingJsonStream",
                EnumSet.of(Modifier.PUBLIC),
                Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JsonReader", "reader"),
                Arrays.asList("IOException")
        );
        emitProxySwitch("return (E) %s.createUsingJsonStream(realm, reader)", writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    // Emits the control flow for selecting the appropriate proxy class based on the model class
    // Currently it is just if..else, which is inefficient for large amounts amounts of model classes.
    // Consider switching to HashMap or similar.
    private void emitProxySwitch(String proxyStatement, JavaWriter writer) throws IOException {
        if (simpleModelClasses.size() == 0) {
            writer.emitStatement("throw new RealmException(%s)", EXCEPTION_MSG);
        } else {
            writer.beginControlFlow("if (clazz.equals(%s.class))", simpleModelClasses.get(0));
            writer.emitStatement(proxyStatement, proxyClasses.get(0));
            for (int i = 1; i < simpleModelClasses.size(); i++) {
                writer.nextControlFlow("else if (clazz.equals(%s.class))", simpleModelClasses.get(i));
                writer.emitStatement(proxyStatement, proxyClasses.get(i));
            }
            writer.nextControlFlow("else");
            writer.emitStatement("throw new RealmException(\"Boom\")", EXCEPTION_MSG);
            writer.endControlFlow();
        }
    }

}