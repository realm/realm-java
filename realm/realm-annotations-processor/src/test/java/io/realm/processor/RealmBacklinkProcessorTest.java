/*
 * Copyright 2017 Realm Inc.
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
package io.realm.processor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;


public class RealmBacklinkProcessorTest {
    private final JavaFileObject sourceClass = JavaFileObjects.forResource("some/test/BacklinkSource.java");
    private final JavaFileObject targetClass = JavaFileObjects.forResource("some/test/BacklinkTarget.java");
    private final JavaFileObject invalidResultsValueType = JavaFileObjects.forResource("some/test/InvalidResultsElementType.java");

    @Test
    public void compileBacklinks() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileSyntheticBacklinks() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass().builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnLinkingObjectsWithInvalidFieldType() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                .type("BacklinkTarget")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("Fields annotated with @LinkingObjects must be RealmResults");
    }

    @Test
    public void failOnLinkingObjectsWithNonFinalField() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // A field with a @LinkingObjects annotation must be final
                .modifiers(Modifier.PUBLIC)
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must be final");
    }

    @Test
    public void failsOnLinkingObjectsWithLinkedFields() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // Defining a backlink more than one levels back is not supported.
                // It can be queried though: `equalTo("selectedFieldParents.selectedFieldParents")
                .clearAnnotations()
                .annotation("LinkingObjects(\"child.id\")")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("The use of '.' to specify fields in referenced classes is not supported");
    }

    @Test
    public void failsOnLinkingObjectsMissingFieldName() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // No backlinked field specified
                .clearAnnotations()
                .annotation("LinkingObjects")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must have a parameter identifying the link target");
    }

    @Test
    public void failsOnLinkingObjectsMissingGeneric() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // No backlink generic param specified
                .type("RealmResults")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must specify a generic type");
    }

    @Test
    public void failsOnLinkingObjectsWithRequiredFields() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // A backlinked field may not be @Required
                .annotation("Required")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("The @LinkingObjects field ");
    }

    @Test
    public void failsOnLinkingObjectsWithIgnoreFields() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // An  @Ignored, backlinked field is completely ignored
                .annotation("Ignore")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    // TODO: This seems like a "gottcha".  We should warn.
    @Test
    public void ignoreStaticLinkingObjects() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                .modifiers(Modifier.PUBLIC, Modifier.STATIC)
                .type("RealmResults")
                .clearAnnotations()
                .annotation("LinkingObjects(\"xxx\")")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failsOnLinkingObjectsFieldNotFound() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // The argument to the @LinkingObjects annotation must name a field in the source class
                .clearAnnotations()
                .annotation("LinkingObjects(\"xxx\")")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("does not exist in class");
    }

    @Test
    public void failsOnLinkingObjectsWithFieldWrongType() throws IOException {
        RealmSyntheticTestClass targetClass = createBacklinkTestClass()
                // The type of the field named in the @LinkingObjects annotation must match
                // the generic type of the annotated field.  BacklinkSource.child is a Backlink,
                // not a Backlinks_WrongType.
                .builder().name("BacklinkTarget_WrongType").build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(sourceClass, targetClass))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("instead of");
    }

    // This method constructs a synthetic Backlink class that *should* compile correctly.
    // It returns the ref to the backlinked Field.  Tests can modify the
    // field in perverse ways, to verify failure modes.
    private RealmSyntheticTestClass.Field createBacklinkTestClass() {
        return new RealmSyntheticTestClass.Builder().name("BacklinkTarget")
                .field().name("id").type("int").builder()
                .field()
                    .name("parents")
                    .type("RealmResults<BacklinkSource>")
                    .modifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .annotation("LinkingObjects(\"child\")")
                    .initializer("null")
                    .hasGetter(false)
                    .hasSetter(false);
    }

    @Test
    public void failToCompileInvalidResultsElementType() {
        ASSERT.about(javaSource())
                .that(invalidResultsValueType)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileBacklinkClassesWithSimpleNameConflicts() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/BacklinkSelfReference.java"),
                        JavaFileObjects.forResource("some/test/conflict/BacklinkSelfReference.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }
}
