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

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;


public class RealmProcessorTest {
    private final JavaFileObject simpleModel = JavaFileObjects.forResource("some/test/Simple.java");
    private final JavaFileObject simpleProxy = JavaFileObjects.forResource("io/realm/some_test_SimpleRealmProxy.java");
    private final JavaFileObject allTypesModel = JavaFileObjects.forResource("some/test/AllTypes.java");
    private final JavaFileObject allTypesProxy = JavaFileObjects.forResource("io/realm/some_test_AllTypesRealmProxy.java");
    private final JavaFileObject allTypesDefaultModule = JavaFileObjects.forResource("io/realm/RealmDefaultModule.java");
    private final JavaFileObject allTypesDefaultMediator = JavaFileObjects.forResource("io/realm/RealmDefaultModuleMediator.java");
    private final JavaFileObject booleansModel = JavaFileObjects.forResource("some/test/Booleans.java");
    private final JavaFileObject booleansProxy = JavaFileObjects.forResource("io/realm/some_test_BooleansRealmProxy.java");
    private final JavaFileObject emptyModel = JavaFileObjects.forResource("some/test/Empty.java");
    private final JavaFileObject finalModel = JavaFileObjects.forResource("some/test/Final.java");
    private final JavaFileObject transientModel = JavaFileObjects.forResource("some/test/Transient.java");
    private final JavaFileObject volatileModel = JavaFileObjects.forResource("some/test/Volatile.java");
    private final JavaFileObject fieldNamesModel = JavaFileObjects.forResource("some/test/FieldNames.java");
    private final JavaFileObject customAccessorModel = JavaFileObjects.forResource("some/test/CustomAccessor.java");
    private final JavaFileObject nullTypesModel = JavaFileObjects.forResource("some/test/NullTypes.java");
    private final JavaFileObject nullTypesProxy = JavaFileObjects.forResource("io/realm/some_test_NullTypesRealmProxy.java");
    private final JavaFileObject missingGenericTypeModel = JavaFileObjects.forResource("some/test/MissingGenericType.java");
    private final JavaFileObject conflictingFieldNameModel = JavaFileObjects.forResource("some/test/ConflictingFieldName.java");
    private final JavaFileObject invalidRealmModelModel_1 = JavaFileObjects.forResource("some/test/InvalidModelRealmModel_1.java");
    private final JavaFileObject invalidRealmModelModel_2 = JavaFileObjects.forResource("some/test/InvalidModelRealmModel_2.java");
    private final JavaFileObject invalidRealmModelModel_3 = JavaFileObjects.forResource("some/test/InvalidModelRealmModel_3.java");
    private final JavaFileObject ValidModelPojo_ExtendingRealmObject = JavaFileObjects.forResource("some/test/ValidModelRealmModel_ExtendingRealmObject.java");
    private final JavaFileObject UseExtendRealmList = JavaFileObjects.forResource("some/test/UseExtendRealmList.java");
    private final JavaFileObject SimpleRealmModel = JavaFileObjects.forResource("some/test/SimpleRealmModel.java");
    private final JavaFileObject customInterface = JavaFileObjects.forResource("some/test/CustomInterface.java");
    private final JavaFileObject nonLatinName = JavaFileObjects.forResource("some/test/ÁrvíztűrőTükörfúrógép.java");

    @Test
    public void compileSimpleFile() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedSimpleFile() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedEmptyFile() {
        ASSERT.about(javaSource())
                .that(emptyModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Ignore("Disabled because it does not seem to find the generated interface file")
    @Test
    public void compileSimpleProxyFile() {
        ASSERT.about(javaSource())
                .that(simpleProxy)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedSimpleFile() {
        ASSERT.about(javaSource())
                .that(simpleModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(simpleProxy);
    }

    @Test
    public void compileProcessedNullTypesFile() {
        ASSERT.about(javaSource())
                .that(nullTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedNullTypesFile() {
        ASSERT.about(javaSource())
                .that(nullTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(nullTypesProxy);
    }

    @Test
    public void compileAllTypesFile() {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedAllTypesFile() {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAllTypesProxyFile() {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedAllTypesFile() {
        ASSERT.about(javaSource())
                .that(allTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(allTypesDefaultMediator, allTypesDefaultModule,
                        allTypesDefaultMediator, allTypesProxy);
    }

    @Test
    public void compileAppModuleCustomClasses() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/AppModuleCustomClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAppModuleAllClasses() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/AppModuleAllClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileLibraryModulesAllClasses() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/LibraryModuleAllClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileLibraryModulesCustomClasses() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/LibraryModuleCustomClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAppModuleMixedParametersFail() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource(
                        "some/test/InvalidAllTypesModuleMixedParameters.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileAppModuleWrongTypeFail() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource(
                        "some/test/InvalidAllTypesModuleWrongType.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileLibraryModuleMixedParametersFail() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/InvalidLibraryModuleMixedParameters.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileLibraryModuleWrongTypeFail() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/InvalidLibraryModuleWrongType.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileBooleanFile() {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedBooleansFile() {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileBooleansProxyFile() {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedBooleansFile() {
        ASSERT.about(javaSource())
                .that(booleansModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(booleansProxy);
    }

    @Test
    public void compileMissingGenericType() {
        ASSERT.about(javaSource())
                .that(missingGenericTypeModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    @Ignore("Disabled because it does not find the generated Interface file")
    public void compileFieldNamesFiles() {
        ASSERT.about(javaSource())
                .that(fieldNamesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileCustomAccessor() {
        ASSERT.about(javaSource())
                .that(customAccessorModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // Supported "Index" annotation types
    @Test
    public void compileIndexTypes() throws IOException {
        final String[] validIndexFieldTypes = {"byte", "short", "int", "long", "boolean", "String", "java.util.Date",
                "Byte", "Short", "Integer", "Long", "Boolean"};

        for (String fieldType : validIndexFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("ValidIndexType").field("testField", fieldType, "Index").build();
            ASSERT.about(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .compilesWithoutError();
        }
    }

    // Unsupported "Index" annotation types
    @Test
    public void compileInvalidIndexTypes() throws IOException {
        final String[] invalidIndexFieldTypes = {"float", "double", "byte[]", "Simple", "RealmList", "Float", "Double"};

        for (String fieldType : invalidIndexFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("InvalidIndexType").field("testField", fieldType, "Index").build();
            ASSERT.about(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .failsToCompile();
        }
    }

    // Supported "PrimaryKey" annotation types
    @Test
    public void compilePrimaryKeyTypes() throws IOException {
        final String[] validPrimaryKeyFieldTypes = {"byte", "short", "int", "long", "String", "Byte", "Short", "Integer", "Long"};

        for (String fieldType : validPrimaryKeyFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("ValidPrimaryKeyType").field("testField", fieldType, "PrimaryKey").build();
            ASSERT.about(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .compilesWithoutError();
        }
    }

    // Unsupported "PrimaryKey" annotation types
    @Test
    public void compileInvalidPrimaryKeyTypes() throws IOException {
        final String[] invalidPrimaryKeyFieldTypes = {"boolean", "java.util.Date", "Simple", "RealmList<Simple>", "Boolean"};

        for (String fieldType : invalidPrimaryKeyFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("InvalidPrimaryKeyType").field("testField", fieldType, "PrimaryKey").build();
            ASSERT.about(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .failsToCompile();
        }
    }

    // Supported "Required" annotation types
    @Test
    public void compileRequiredTypes() throws IOException {
        final String[] validPrimaryKeyFieldTypes = {"Byte", "Short", "Integer", "Long", "String",
                "Float", "Double", "Boolean", "java.util.Date"};

        for (String fieldType : validPrimaryKeyFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("ValidRequiredType").field("testField", fieldType, "Required").build();
            ASSERT.about(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .compilesWithoutError();
        }
    }

    // Not supported "Required" annotation types
    @Test
    public void compileInvalidRequiredTypes() throws IOException {
        final String[] invalidRequiredAnnotationFieldTypes = {"byte", "short", "int", "long", "float", "double",
                "boolean", "RealmList<Simple>", "Simple"};

        for (String fieldType : invalidRequiredAnnotationFieldTypes) {
            RealmSyntheticTestClass javaFileObject = new RealmSyntheticTestClass.Builder()
                    .name("InvalidRequiredType")
                    .field("testField", fieldType, "Required")
                    .build();
            ASSERT.about(javaSources())
                    .that(Arrays.asList(simpleModel, javaFileObject))
                    .processedWith(new RealmProcessor())
                    .failsToCompile();
        }
    }

    @Test
    public void compileConflictingFieldName() {
        ASSERT.about(javaSource())
                .that(conflictingFieldNameModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnFinalFields() {
        ASSERT.about(javaSource())
                .that(finalModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileTransientFields() {
        ASSERT.about(javaSource())
                .that(transientModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnVolatileFields() {
        ASSERT.about(javaSource())
                .that(volatileModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // annotation without implementing RealmModel interface
    @Test
    public void failOnInvalidRealmModel_1() {
        ASSERT.about(javaSource())
                .that(invalidRealmModelModel_1)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // it's not allowed to extend from another RealmObject
    @Test
    public void failOnInvalidRealmModel_2() {
        ASSERT.about(javaSource())
                .that(invalidRealmModelModel_2)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // it's not allowed to extend from another RealmObject
    @Test
    public void failOnInvalidRealmModel_3() {
        ASSERT.about(javaSource())
                .that(invalidRealmModelModel_3)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void validRealmModelUsingInheritance() {
        ASSERT.about(javaSource())
                .that(ValidModelPojo_ExtendingRealmObject)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void canNotInheritRealmList() {
        ASSERT.about(javaSource())
                .that(UseExtendRealmList)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileWithRealmModelFieldInReamlModel() {
        ASSERT.about(javaSource())
                .that(SimpleRealmModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileWithInterfaceForList() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(JavaFileObjects.forResource("some/test/InterfaceList.java"), customInterface))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileWithInterfaceForObject() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(JavaFileObjects.forResource("some/test/InterfaceObjectReference.java"), customInterface))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compareNonLatinName() {
        ASSERT.about(javaSource())
                .that(nonLatinName)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }
}
