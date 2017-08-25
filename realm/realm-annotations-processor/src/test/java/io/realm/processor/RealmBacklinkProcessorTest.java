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

import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;


public class RealmBacklinkProcessorTest {
    private final JavaFileObject backlinks = JavaFileObjects.forResource("some/test/Backlinks.java");
    private final JavaFileObject backlinksTarget = JavaFileObjects.forResource("some/test/BacklinkTarget.java");

    @Test
    public void compileBacklinks() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileSyntheticBacklinks() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnLinkingObjectsWithInvalidFieldType() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // Backlinks must be RealmResults
                .type("BacklinkTarget")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("Fields annotated with @LinkingObjects must be RealmResults");
    }

    @Test
    public void failOnLinkingObjectsWithNonFinalField() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // A field with a @LinkingObjects annotation must be final
                .modifiers(Modifier.PUBLIC)
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must be final");
    }

    @Test
    public void failsOnLinkingObjectsWithLinkedFields() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // Defining a backlink more than one levels back is not supported.
                // It can be queried though: `equalTo("selectedFieldParents.selectedFieldParents")
                .clearAnnotations()
                .annotation("LinkingObjects(\"child.id\")")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("The use of '.' to specify fields in referenced classes is not supported");
    }

    @Test
    public void failsOnLinkingObjectsMissingFieldName() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // No backlinked field specified
                .clearAnnotations()
                .annotation("LinkingObjects")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must have a parameter identifying the link target");
    }

    @Test
    public void failsOnLinkingObjectsMissingGeneric() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // No backlink generic param specified
                .type("RealmResults")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must specify a generic type");
    }

    @Test
    public void failsOnLinkingObjectsWithRequiredFields() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // A backlinked field may not be @Required
                .annotation("Required")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("cannot be @Required");
    }

    @Test
    public void failsOnLinkingObjectsWithIgnoreFields() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // An  @Ignored, backlinked field is completely ignored
                .annotation("Ignore")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    // TODO: This seems like a "gottcha".  We should warn.
    @Test
    public void ignoreStaticLinkingObjects() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                .modifiers(Modifier.PUBLIC, Modifier.STATIC)
                .type("RealmResults")
                .clearAnnotations()
                .annotation("LinkingObjects(\"xxx\")")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failsOnLinkingObjectsFieldNotFound() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // The argument to the @LinkingObjects annotation must name a field in the target class
                .clearAnnotations()
                .annotation("LinkingObjects(\"xxx\")")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("does not exist in class");
    }

    @Test
    public void failsOnLinkingObjectsWithFieldWrongType() throws IOException {
        RealmSyntheticTestClass javaFileObject = createBacklinkTestClass()
                // The type of the field named in the @LinkingObjects annotation must match
                // the generic type of the annotated field.  BacklinkTarget.child is a Backlink,
                // not a Backlinks_WrongType.
                .builder().name("Backlinks_WrongType").build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinksTarget, javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("instead of");
    }

    // This method constructs a synthetic Backlink class that *should* compile correctly.
    // It returns the ref to the backlinked Field.  Tests can modify the
    // field in perverse ways, to verify failure modes.
    private RealmSyntheticTestClass.Field createBacklinkTestClass() {
        return new RealmSyntheticTestClass.Builder().name("Backlinks")
                .field().name("id").type("int").builder()
                .field()
                    .name("parents")
                    .type("RealmResults<BacklinkTarget>")
                    .modifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .annotation("LinkingObjects(\"child\")")
                    .initializer("null")
                    .hasGetter(false)
                    .hasSetter(false);
    }
}
