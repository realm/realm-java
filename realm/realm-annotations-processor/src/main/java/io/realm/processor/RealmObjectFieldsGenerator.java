package io.realm.processor;

import com.squareup.javawriter.JavaWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.EnumSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;


/**
 * Generates a Fields class for each class annotated with @RealmClass
 * Example:
 * <pre>
 * {@code
 *
 *   public class Person extends RealmObject {
 *
 *      private int id;
 *      private String name;
 *   }
 *
 *   // Generated class.
 *   public final class PersonFields {
 *       public static final String ID = "id";
 *       public static final String NAME = "name";
 *   }
 *
 *   // Query the person with an id of 1.
 *   realm.where(Person.class).equalTo(PersonFields.ID, 1).findFirst();
 * }
 * </pre>
 *
 * @author Raee, Mulham (mulham.raee@gmail.com)
 */
public class RealmObjectFieldsGenerator {
    private ProcessingEnvironment processingEnvironment;
    private ClassMetaData metaData;
    private final String simpleClassName;

    public RealmObjectFieldsGenerator(ProcessingEnvironment processingEnvironment, ClassMetaData metaData) {
        this.processingEnvironment = processingEnvironment;
        this.metaData = metaData;
        this.simpleClassName = metaData.getSimpleClassName();
    }

    public void generate() throws IOException {
        String qualifiedGeneratedInterfaceName =
                String.format("%s.%s", Constants.REALM_PACKAGE_NAME, Utils.getFieldsClassName(simpleClassName));
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedInterfaceName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));

        writer.setIndent(Constants.INDENT);

        writer
                .emitPackage(Constants.REALM_PACKAGE_NAME)
                .emitEmptyLine()
                .beginType(qualifiedGeneratedInterfaceName, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        for (VariableElement element : metaData.getFields()) {
            String originalFieldName = element.getSimpleName().toString();
            String generatedFieldName = generateFieldName(originalFieldName);

            writer
                    .emitField("String", generatedFieldName, EnumSet.of(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC), originalFieldName);

            QueryFieldBy queryFieldBy = element.getAnnotation(QueryFieldBy.class);
            if (queryFieldBy != null) {

                for (String field : queryFieldBy.fields()) {
                    if (field.length() == 0) {
                        continue;
                    }

                    writer
                            .emitField("String", generatedFieldName + "_" + generateFieldName(field),
                                    EnumSet.of(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC), originalFieldName + "." + field);
                }
            }


        }

        writer.endType();
        writer.close();
    }

    private String generateFieldName(String str) {
        StringBuilder builder = new StringBuilder(str);
        for (int i = str.length() - 1; i >= 0; i--) {
            if (Character.isUpperCase(str.charAt(i))) {
                builder.insert(i, '_');
            }
        }
        return builder.toString().toUpperCase();
    }

}
