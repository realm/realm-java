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

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;


public class RealmProcessorTest {
    private final JavaFileObject simpleModel = JavaFileObjects.forResource("some/test/Simple.java");
    private final JavaFileObject simpleProxy = JavaFileObjects.forResource("io/realm/some_test_SimpleRealmProxy.java");
    private final JavaFileObject allTypesModel = JavaFileObjects.forResource("some/test/AllTypes.java");
    private final JavaFileObject allTypesProxy = JavaFileObjects.forResource("io/realm/some_test_AllTypesRealmProxy.java");
    private final JavaFileObject allTypesDefaultModule = JavaFileObjects.forResource("io/realm/DefaultRealmModule.java");
    private final JavaFileObject allTypesDefaultMediator = JavaFileObjects.forResource("io/realm/DefaultRealmModuleMediator.java");
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
    private final JavaFileObject realmMapModel = JavaFileObjects.forResource("some/test/RealmMapModel.java");
    private final JavaFileObject realmDictionaryMissingGenericsModel = JavaFileObjects.forResource("some/test/RealmDictionaryMissingGenerics.java");
    private final JavaFileObject realmDictionaryModel = JavaFileObjects.forResource("some/test/RealmDictionaryModel.java");
    private final JavaFileObject realmDictionaryModelWrongType = JavaFileObjects.forResource("some/test/RealmDictionaryModelWrongType.java");

    @Test
    public void compileSimpleFile() {
        assertAbout(javaSource())
                .that(simpleModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedSimpleFile() {
        assertAbout(javaSource())
                .that(simpleModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedEmptyFile() {
        assertAbout(javaSource())
                .that(emptyModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Ignore("Disabled because it does not seem to find the generated interface file")
    @Test
    public void compileSimpleProxyFile() {
        assertAbout(javaSource())
                .that(simpleProxy)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedSimpleFile() {
        assertAbout(javaSource())
                .that(simpleModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(simpleProxy);
    }

    @Test
    public void compileProcessedNullTypesFile() {
        assertAbout(javaSource())
                .that(nullTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedNullTypesFile() {
        assertAbout(javaSource())
                .that(nullTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(nullTypesProxy);
    }

    @Test
    public void compileAllTypesFile() {
        assertAbout(javaSource())
                .that(allTypesModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedAllTypesFile() {
        assertAbout(javaSource())
                .that(allTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAllTypesProxyFile() {
        assertAbout(javaSource())
                .that(allTypesModel)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedAllTypesFile() {
        assertAbout(javaSource())
                .that(allTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(allTypesDefaultModule);
    }

    @Test
    public void compileAppModuleCustomClasses() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/AppModuleCustomClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAppModuleAllClasses() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/AppModuleAllClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileLibraryModulesAllClasses() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/LibraryModuleAllClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileLibraryModulesCustomClasses() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/LibraryModuleCustomClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAppModuleMixedParametersFail() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource(
                        "some/test/InvalidAllTypesModuleMixedParameters.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileAppModuleWrongTypeFail() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource(
                        "some/test/InvalidAllTypesModuleWrongType.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileLibraryModuleMixedParametersFail() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/InvalidLibraryModuleMixedParameters.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileLibraryModuleWrongTypeFail() {
        assertAbout(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/InvalidLibraryModuleWrongType.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileBooleanFile() {
        assertAbout(javaSource())
                .that(booleansModel)
                .compilesWithoutError();
    }

    @Test
    public void compileProcessedBooleansFile() {
        assertAbout(javaSource())
                .that(booleansModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileBooleansProxyFile() {
        assertAbout(javaSource())
                .that(booleansModel)
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedBooleansFile() {
        assertAbout(javaSource())
                .that(booleansModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(booleansProxy);
    }

    @Test
    public void compileMissingGenericType() {
        assertAbout(javaSource())
                .that(missingGenericTypeModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    @Ignore("Disabled because it does not find the generated Interface file")
    public void compileFieldNamesFiles() {
        assertAbout(javaSource())
                .that(fieldNamesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileCustomAccessor() {
        assertAbout(javaSource())
                .that(customAccessorModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // Supported "Index" annotation types
    @Test
    public void compileIndexTypes() throws IOException {
        final String[] validIndexFieldTypes = {"byte", "short", "int", "long", "boolean", "String", "java.util.Date",
                "Byte", "Short", "Integer", "Long", "Boolean", "org.bson.types.ObjectId", "java.util.UUID"};

        for (String fieldType : validIndexFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("ValidIndexType").field("testField", fieldType, "Index").build();
            assertAbout(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .compilesWithoutError();
        }
    }

    // Unsupported "Index" annotation types
    @Test
    public void compileInvalidIndexTypes() throws IOException {
        final String[] invalidIndexFieldTypes = {"float", "double", "byte[]", "Simple", "RealmList", "Float", "Double", "org.bson.types.Decimal128"};

        for (String fieldType : invalidIndexFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("InvalidIndexType").field("testField", fieldType, "Index").build();
            assertAbout(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .failsToCompile();
        }
    }

    // Supported "PrimaryKey" annotation types
    @Test
    public void compilePrimaryKeyTypes() throws IOException {
        final String[] validPrimaryKeyFieldTypes = {"byte", "short", "int", "long", "String", "Byte", "Short", "Integer", "Long", "org.bson.types.ObjectId", "java.util.UUID"};

        for (String fieldType : validPrimaryKeyFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("ValidPrimaryKeyType").field("testField", fieldType, "PrimaryKey").build();
            assertAbout(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .compilesWithoutError();
        }
    }

    // Unsupported "PrimaryKey" annotation types
    @Test
    public void compileInvalidPrimaryKeyTypes() throws IOException {
        final String[] invalidPrimaryKeyFieldTypes = {"boolean", "java.util.Date", "Simple", "RealmList<Simple>", "Boolean", "org.bson.types.Decimal128"};

        for (String fieldType : invalidPrimaryKeyFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("InvalidPrimaryKeyType").field("testField", fieldType, "PrimaryKey").build();
            assertAbout(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .failsToCompile();
        }
    }

    // Supported "Required" annotation types
    @Test
    public void compileRequiredTypes() throws IOException {
        final String[] validPrimaryKeyFieldTypes = {"Byte", "Short", "Integer", "Long", "String",
                "Float", "Double", "Boolean", "java.util.Date", "org.bson.types.ObjectId", "org.bson.types.Decimal128", "java.util.UUID"};

        for (String fieldType : validPrimaryKeyFieldTypes) {
            RealmSyntheticTestClass javaFileObject =
                    new RealmSyntheticTestClass.Builder().name("ValidRequiredType").field("testField", fieldType, "Required").build();
            assertAbout(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .compilesWithoutError();
        }
    }

    // Not supported "Required" annotation types
    @Test
    public void compileInvalidRequiredTypes() throws IOException {
        final String[] invalidRequiredAnnotationFieldTypes = {"byte", "short", "int", "long", "float", "double",
                "boolean", "RealmList<Simple>", "Simple", "Mixed", "RealmList<Mixed>"};

        for (String fieldType : invalidRequiredAnnotationFieldTypes) {
            RealmSyntheticTestClass javaFileObject = new RealmSyntheticTestClass.Builder()
                    .name("InvalidRequiredType")
                    .field("testField", fieldType, "Required")
                    .build();
            assertAbout(javaSources())
                    .that(Arrays.asList(simpleModel, javaFileObject))
                    .processedWith(new RealmProcessor())
                    .failsToCompile();
        }
    }

    @Test
    public void compileConflictingFieldName() {
        assertAbout(javaSource())
                .that(conflictingFieldNameModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnFinalFields() {
        assertAbout(javaSource())
                .that(finalModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileTransientFields() {
        assertAbout(javaSource())
                .that(transientModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnVolatileFields() {
        assertAbout(javaSource())
                .that(volatileModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // annotation without implementing RealmModel interface
    @Test
    public void failOnInvalidRealmModel_1() {
        assertAbout(javaSource())
                .that(invalidRealmModelModel_1)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // it's not allowed to extend from another RealmObject
    @Test
    public void failOnInvalidRealmModel_2() {
        assertAbout(javaSource())
                .that(invalidRealmModelModel_2)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // it's not allowed to extend from another RealmObject
    @Test
    public void failOnInvalidRealmModel_3() {
        assertAbout(javaSource())
                .that(invalidRealmModelModel_3)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void validRealmModelUsingInheritance() {
        assertAbout(javaSource())
                .that(ValidModelPojo_ExtendingRealmObject)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void canNotInheritRealmList() {
        assertAbout(javaSource())
                .that(UseExtendRealmList)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileWithRealmModelFieldInReamlModel() {
        assertAbout(javaSource())
                .that(SimpleRealmModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileWithInterfaceForList() {
        assertAbout(javaSources())
                .that(Arrays.asList(JavaFileObjects.forResource("some/test/InterfaceList.java"), customInterface))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileWithInterfaceForObject() {
        assertAbout(javaSources())
                .that(Arrays.asList(JavaFileObjects.forResource("some/test/InterfaceObjectReference.java"), customInterface))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compareNonLatinName() {
        assertAbout(javaSource())
                .that(nonLatinName)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileRealmMapModelNotAllowed() {
        ASSERT.about(javaSource())
                .that(realmMapModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileRealmDictionaryMissingGenerics() {
        ASSERT.about(javaSource())
                .that(realmDictionaryMissingGenericsModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileRealmDictionaryModel() {
        ASSERT.about(javaSource())
                .that(realmDictionaryModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileRealmDictionaryModelWrongType() {
        ASSERT.about(javaSource())
                .that(realmDictionaryModelWrongType)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }
}
