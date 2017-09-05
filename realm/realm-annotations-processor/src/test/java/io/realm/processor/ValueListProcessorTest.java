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

import org.junit.Ignore;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;


public class ValueListProcessorTest {
    private final JavaFileObject valueList = JavaFileObjects.forResource("some/test/ValueList.java");
    private final JavaFileObject invalidListValueType = JavaFileObjects.forResource("some/test/InvalidListElementType.java");

    @Test
    @Ignore("need to implement primitive list support in realm-library")
    public void compileValueList() {
        ASSERT.about(javaSource())
                .that(valueList)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failToCompileInvalidListElementType() {
        ASSERT.about(javaSource())
                .that(invalidListValueType)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }
}
