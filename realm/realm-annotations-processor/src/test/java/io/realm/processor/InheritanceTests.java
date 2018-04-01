/*
 * Copyright 2018 Realm Inc.
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

import java.util.Arrays;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;

public class InheritanceTests {

    @Test
    public void compileOnlyBaseClass() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("some/test/Base.java"))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileBaseAndSubClass() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/Base.java"),
                        JavaFileObjects.forResource("some/test/Sub.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileEmptySuperClass() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/EmptyBase.java"),
                        JavaFileObjects.forResource("some/test/SubWithEmptyBase.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failToCompileListReferenceToBaseClass() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/Base.java"),
                        JavaFileObjects.forResource("some/test/AbstractClassesNotAllowedInList.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("Only concrete Realm model classes can be used as a generic argument");
    }

    @Test
    public void failToCompileResultsReferenceToBaseClass() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/Base.java"),
                        JavaFileObjects.forResource("some/test/AbstractClassesNotAllowedInResults.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("can only reference concrete classes as generic parameters and not abstract super classes");
    }

    @Test
    public void failToCompileClassThatExtendsNonAbstractSuperClass() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/Base.java"),
                        JavaFileObjects.forResource("some/test/Sub.java"),
                        JavaFileObjects.forResource("some/test/SubSub.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("Only abstract super classes are allowed");
    }

    @Test
    public void compareProcessedSubClass() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/Base.java"),
                        JavaFileObjects.forResource("some/test/Sub.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("io/realm/some_test_SubRealmProxy.java"));
    }

    @Test
    public void compareProcessedSubClassWithOverrides() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/Base.java"),
                        JavaFileObjects.forResource("some/test/Sub.java"),
                        JavaFileObjects.forResource("some/test/SubWithOverrides.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("io/realm/some_test_SubWithOverridesRealmProxy.java"));
    }

}
