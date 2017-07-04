/*
 * Copyright 2014-2016 Realm Inc.
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

    private JavaFileObject simpleModel = JavaFileObjects.forResource("some/test/Simple.java");
    private JavaFileObject simpleProxy = JavaFileObjects.forResource("io/realm/SimpleRealmProxy.java");
    private JavaFileObject allTypesModel = JavaFileObjects.forResource("some/test/AllTypes.java");
    private JavaFileObject allTypesProxy = JavaFileObjects.forResource("io/realm/AllTypesRealmProxy.java");
    private JavaFileObject allTypesDefaultModule = JavaFileObjects.forResource("io/realm/RealmDefaultModule.java");
    private JavaFileObject allTypesDefaultMediator = JavaFileObjects.forResource("io/realm/RealmDefaultModuleMediator.java");
    private JavaFileObject booleansModel = JavaFileObjects.forResource("some/test/Booleans.java");
    private JavaFileObject booleansProxy = JavaFileObjects.forResource("io/realm/BooleansRealmProxy.java");
    private JavaFileObject emptyModel = JavaFileObjects.forResource("some/test/Empty.java");
    private JavaFileObject finalModel = JavaFileObjects.forResource("some/test/Final.java");
    private JavaFileObject transientModel = JavaFileObjects.forResource("some/test/Transient.java");
    private JavaFileObject volatileModel = JavaFileObjects.forResource("some/test/Volatile.java");
    private JavaFileObject fieldNamesModel = JavaFileObjects.forResource("some/test/FieldNames.java");
    private JavaFileObject customAccessorModel = JavaFileObjects.forResource("some/test/CustomAccessor.java");
    private JavaFileObject nullTypesModel = JavaFileObjects.forResource("some/test/NullTypes.java");
    private JavaFileObject nullTypesProxy = JavaFileObjects.forResource("io/realm/NullTypesRealmProxy.java");
    private JavaFileObject missingGenericTypeModel = JavaFileObjects.forResource("some/test/MissingGenericType.java");
    private JavaFileObject conflictingFieldNameModel = JavaFileObjects.forResource("some/test/ConflictingFieldName.java");
    private JavaFileObject invalidRealmModelModel_1 = JavaFileObjects.forResource("some/test/InvalidModelRealmModel_1.java");
    private JavaFileObject invalidRealmModelModel_2 = JavaFileObjects.forResource("some/test/InvalidModelRealmModel_2.java");
    private JavaFileObject invalidRealmModelModel_3 = JavaFileObjects.forResource("some/test/InvalidModelRealmModel_3.java");
    private JavaFileObject ValidModelPojo_ExtendingRealmObject = JavaFileObjects.forResource("some/test/ValidModelRealmModel_ExtendingRealmObject.java");
    private JavaFileObject UseExtendRealmList = JavaFileObjects.forResource("some/test/UseExtendRealmList.java");
    private JavaFileObject SimpleRealmModel = JavaFileObjects.forResource("some/test/SimpleRealmModel.java");
    private JavaFileObject customInterface = JavaFileObjects.forResource("some/test/CustomInterface.java");
    private JavaFileObject backlinks = JavaFileObjects.forResource("some/test/Backlinks.java");
    private JavaFileObject backlinksTarget = JavaFileObjects.forResource("some/test/BacklinkTarget.java");
    private JavaFileObject backlinksInvalidField = JavaFileObjects.forResource("some/test/Backlinks_InvalidFieldType.java");
    private JavaFileObject backlinksLinked = JavaFileObjects.forResource("some/test/Backlinks_LinkedFields.java");
    private JavaFileObject backlinksMissingParam = JavaFileObjects.forResource("some/test/Backlinks_MissingParameter.java");
    private JavaFileObject backlinksMissingGeneric = JavaFileObjects.forResource("some/test/Backlinks_MissingGeneric.java");
    private JavaFileObject backlinksRequired = JavaFileObjects.forResource("some/test/Backlinks_Required.java");
    private JavaFileObject backlinksIgnored = JavaFileObjects.forResource("some/test/Backlinks_Ignored.java");
    private JavaFileObject backlinksNotFound = JavaFileObjects.forResource("some/test/Backlinks_NotFound.java");
    private JavaFileObject backlinksNonFinalField = JavaFileObjects.forResource("some/test/Backlinks_NotFinal.java");
    private JavaFileObject backlinksWrongType = JavaFileObjects.forResource("some/test/Backlinks_WrongType.java");
    private JavaFileObject nonLatinName = JavaFileObjects.forResource("some/test/ÁrvíztűrőTükörfúrógép.java");

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
    public void compileProcessedEmptyFile() throws Exception {
        ASSERT.about(javaSource())
                .that(emptyModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // Disabled because it does not seem to find the generated interface file @Test
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
    public void compileProcessedNullTypesFile() throws Exception {
        ASSERT.about(javaSource())
                .that(nullTypesModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compareProcessedNullTypesFile() throws Exception {
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
                .generatesSources(allTypesDefaultMediator, allTypesDefaultModule,
                        allTypesDefaultMediator, allTypesProxy);
    }

    @Test
    public void compileAppModuleCustomClasses() throws Exception {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/AppModuleCustomClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAppModuleAllClasses() throws Exception {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/AppModuleAllClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileLibraryModulesAllClasses() throws Exception {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/LibraryModuleAllClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileLibraryModulesCustomClasses() throws Exception {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/LibraryModuleCustomClasses.java")))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void compileAppModuleMixedParametersFail() throws Exception {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource(
                        "some/test/InvalidAllTypesModuleMixedParameters.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileAppModuleWrongTypeFail() throws Exception {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource(
                        "some/test/InvalidAllTypesModuleWrongType.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileLibraryModuleMixedParametersFail() throws Exception {
        ASSERT.about(javaSources())
                .that(Arrays.asList(allTypesModel, JavaFileObjects.forResource("some/test/InvalidLibraryModuleMixedParameters.java")))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileLibraryModuleWrongTypeFail() throws Exception {
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
            TestRealmObjectFileObject javaFileObject =
                    TestRealmObjectFileObject.getSingleFieldInstance("ValidIndexType", "Index", fieldType, "testField");
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
            TestRealmObjectFileObject javaFileObject = TestRealmObjectFileObject.getSingleFieldInstance(
                    "InvalidIndexType", "Index", fieldType, "testField");
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
            TestRealmObjectFileObject javaFileObject = TestRealmObjectFileObject.getSingleFieldInstance(
                    "ValidPrimaryKeyType", "PrimaryKey", fieldType, "testField");
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
            TestRealmObjectFileObject javaFileObject =
                    TestRealmObjectFileObject.getSingleFieldInstance(
                            "InvalidPrimaryKeyType", "PrimaryKey", fieldType, "testField");
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
            TestRealmObjectFileObject javaFileObject = TestRealmObjectFileObject.getSingleFieldInstance(
                    "ValidPrimaryKeyType", "Required", fieldType, "testField");
            ASSERT.about(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .compilesWithoutError();
        }
    }

    // Not supported "Required" annotation types
    @Test
    public void compileInvalidRequiredTypes() throws IOException {
        final String[] validPrimaryKeyFieldTypes = {"byte", "short", "int", "long", "float", "double",
                "boolean", "RealmList<Simple>", "Simple"};

        for (String fieldType : validPrimaryKeyFieldTypes) {
            TestRealmObjectFileObject javaFileObject = TestRealmObjectFileObject.getSingleFieldInstance(
                    "ValidPrimaryKeyType", "Required", fieldType, "testField");
            ASSERT.about(javaSource())
                    .that(javaFileObject)
                    .processedWith(new RealmProcessor())
                    .failsToCompile();
        }
    }

    @Test
    public void compileConflictingFieldName() throws Exception {
        ASSERT.about(javaSource())
                .that(conflictingFieldNameModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnFinalFields() throws Exception {
        ASSERT.about(javaSource())
                .that(finalModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void compileTransientFields() throws Exception {
        ASSERT.about(javaSource())
                .that(transientModel)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnVolatileFields() throws Exception {
        ASSERT.about(javaSource())
                .that(volatileModel)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // annotation without implementing RealmModel interface
    @Test
    public void failOnInvalidRealmModel_1() throws Exception {
        ASSERT.about(javaSource())
                .that(invalidRealmModelModel_1)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // it's not allowed to extend from another RealmObject
    @Test
    public void failOnInvalidRealmModel_2() throws Exception {
        ASSERT.about(javaSource())
                .that(invalidRealmModelModel_2)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    // it's not allowed to extend from another RealmObject
    @Test
    public void failOnInvalidRealmModel_3() throws Exception {
        ASSERT.about(javaSource())
                .that(invalidRealmModelModel_3)
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }

    @Test
    public void validRealmModelUsingInheritance() throws Exception {
        ASSERT.about(javaSource())
                .that(ValidModelPojo_ExtendingRealmObject)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void canNotInheritRealmList() throws Exception {
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
    public void compileBacklinks() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failOnLinkingObjectsWithInvalidFieldType() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksInvalidField))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("Fields annotated with @LinkingObjects must be RealmResults");
    }

    @Test
    public void failOnLinkingObjectsWithNonFinalField() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksNonFinalField))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must be final");
    }

    @Test
    public void failsOnLinkingObjectsWithLinkedFields() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksLinked))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("The use of '.' to specify fields in referenced classes is not supported");
    }

    @Test
    public void failsOnLinkingObjectsMissingFieldName() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksMissingParam))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must have a parameter identifying the link target");
    }

    @Test
    public void failsOnLinkingObjectsMissingGeneric() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksMissingGeneric))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("must specify a generic type");
    }

    @Test
    public void failsOnLinkingObjectsWithRequiredFields() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksRequired))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("cannot be @Required");
    }

    @Test
    public void failsOnLinkingObjectsWithIgnoreFields() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksIgnored))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void failsOnLinkingObjectsFieldNotFound() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksNotFound))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("does not exist in class");
    }

    @Test
    public void failsOnLinkingObjectsWithFieldWrongType() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(backlinks, backlinksTarget, backlinksWrongType))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("instead of");
    }

    @Test
    public void compareNonLatinName() throws Exception {
        ASSERT.about(javaSource())
                .that(nonLatinName)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }
}
