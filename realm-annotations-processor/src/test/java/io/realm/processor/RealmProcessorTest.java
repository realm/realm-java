/*
 * Copyright 2014 Realm Inc.
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

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class RealmProcessorTest {

    private JavaFileObject simpleModel = JavaFileObjects.forResource("some/test/Simple.java");
    private JavaFileObject simpleProxy = JavaFileObjects.forResource("io/realm/SimpleRealmProxy.java");
    private JavaFileObject allTypesModel = JavaFileObjects.forResource("some/test/AllTypes.java");
    private JavaFileObject allTypesProxy = JavaFileObjects.forResource("io/realm/AllTypesRealmProxy.java");
    private JavaFileObject booleansModel = JavaFileObjects.forResource("some/test/Booleans.java");
    private JavaFileObject booleansProxy = JavaFileObjects.forResource("io/realm/BooleansRealmProxy.java");

    @Test
    public void compileSimpleFile() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedSimpleFile() throws Exception {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileSimpleProxyFile() throws Exception {
        ASSERT.about(javaSource())
                .that(simpleProxy)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedSimpleFile() throws Exception {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(simpleProxy);
    }

    @Test
    public void compileAllTypesFile() {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedAllTypesFile() throws Exception {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAllTypesProxyFile() throws Exception {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedAllTypesFile() throws Exception {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(allTypesProxy);
    }

    @Test
    public void compileBooleanFile() {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedBooleansFile() throws Exception {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileBooleansProxyFile() throws Exception {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedBooleansFile() throws Exception {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(booleansProxy);
    }
}
