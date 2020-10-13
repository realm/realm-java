package io.realm.processor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Arrays;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;

public class RealmEmbeddedObjectsTest {

    @Test
    public void compileAndCompareEmbeddedObjectFile() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("some/test/EmbeddedClass.java"))
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("io/realm/some_test_EmbeddedClassRealmProxy.java"));
    }

    @Test
    public void compileAndCompareParentToEmbeddedObjectFile() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/EmbeddedClassSimpleParent.java"),
                        JavaFileObjects.forResource("some/test/EmbeddedClass.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("io/realm/some_test_EmbeddedClassSimpleParentRealmProxy.java"));
    }

    @Test
    public void compileWithSingleRequiredParent() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                    JavaFileObjects.forResource("some/test/EmbeddedClassParent.java"),
                    JavaFileObjects.forResource("some/test/EmbeddedClass.java"),
                    JavaFileObjects.forResource("some/test/EmbeddedClassOptionalParents.java"),
                    JavaFileObjects.forResource("some/test/EmbeddedClassRequiredParent.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }


    @Test
    public void compileWithMultipleOptionalParents() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/EmbeddedClassParent.java"),
                        JavaFileObjects.forResource("some/test/EmbeddedClass.java"),
                        JavaFileObjects.forResource("some/test/EmbeddedClassRequiredParent.java"),
                        JavaFileObjects.forResource("some/test/EmbeddedClassOptionalParents.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failToCompileIfSingleParentIsMissingFinal() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/EmbeddedClassParent.java"),
                        JavaFileObjects.forResource("some/test/EmbeddedClassMissingFinalOnLinkingObjects.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("The @LinkingObjects field \"some.test.EmbeddedClassMissingFinalOnLinkingObjects.parent\" must be final.");
    }

    // If a single parent type has multiple potential fields that can act as parent. Any
    // @LinkingObject field in the child must designate the field name in the parent.
    @Test
    public void failToCompileIfMissingFieldDescriptor() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/EmbeddedClassParent.java"),
                        JavaFileObjects.forResource("some/test/EmbeddedClassMissingFieldDescription.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("The @LinkingObjects annotation for the field \"some.test.EmbeddedClassMissingFieldDescription.parent1\" must have a parameter identifying the link target.");
    }


    // @PrimaryKey is not allowed inside embedded classes
    @Test
    public void failToCompileWithPrimaryKey() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/EmbeddedClassPrimaryKey.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("A model class marked as embedded cannot contain a @PrimaryKey.");
    }

    // If a child has multiple potential parents, none of them are allowed to be marked
    // @Required.
    @Test
    public void failToCompileWithMultipleRequiredParents() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/EmbeddedClassParent.java"),
                        JavaFileObjects.forResource("some/test/EmbeddedClassMultipleRequiredParents.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("@Required cannot be used on @LinkingObjects field if multiple @LinkingParents are defined");
    }
}
