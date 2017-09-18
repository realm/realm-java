/*
 * Copyright 2014-2017 Realm Inc.
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

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;


public class RealmCounterProcessorTest {

    @Test
    public void compileMutableRealmInteger() throws IOException {
        RealmSyntheticTestClass javaFileObject = createCounterTestClass()
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileIgnoredMutableRealmInteger() throws IOException {
        RealmSyntheticTestClass javaFileObject = createCounterTestClass()
                .annotation("Ignore")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileIndexedMutableRealmInteger() throws IOException {
        RealmSyntheticTestClass javaFileObject = createCounterTestClass()
                .annotation("Index")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileRequiredMutableRealmInteger() throws IOException {
        RealmSyntheticTestClass javaFileObject = createCounterTestClass()
                .annotation("Required")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileStaticMutableRealmInteger() throws IOException {
        RealmSyntheticTestClass javaFileObject = createCounterTestClass()
                .modifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(javaFileObject))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnPKMutableRealmInteger() throws IOException {
        RealmSyntheticTestClass javaFileObject = createCounterTestClass()
                .annotation("PrimaryKey")
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("cannot be used as primary key");
    }

    @Test
    public void failUnlessFinalMutableRealmInteger() throws IOException {
        RealmSyntheticTestClass javaFileObject = createCounterTestClass()
                .modifiers(Modifier.PRIVATE)
                .builder().build();
        ASSERT.about(javaSources())
                .that(Arrays.asList(javaFileObject))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must be final");
    }

    // This method constructs a synthetic Counter test class that *should* compile correctly.
    // It returns the ref to the Counter Field.  Tests can modify the
    // field in perverse ways, to verify failure modes.
    private RealmSyntheticTestClass.Field createCounterTestClass() {
        return new RealmSyntheticTestClass.Builder().name("Counter")
                .field().name("id").type("int").builder()
                .field()
                .name("columnMutableRealmInteger")
                .type("MutableRealmInteger")
                .modifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("MutableRealmInteger.valueOf(0)")
                .hasSetter(false);
    }
}
