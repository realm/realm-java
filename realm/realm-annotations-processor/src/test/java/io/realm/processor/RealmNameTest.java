package io.realm.processor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Arrays;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;

public class RealmNameTest {

    // Check that a class only with class name policy compiles
    @Test
    public void compileOnlyClassNamePolicyFile() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("some/test/NamePolicyClassOnly.java"))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    // Check that a class only with a field name policy compiles
    @Test
    public void compileOnlyFieldNamePolicyFile() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("some/test/NamePolicyFieldNameOnly.java"))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    // Check that things compile if there is only a module with name policies defined
    @Test
    public void compileModuleWithNamePolicyFile() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("some/test/NamePolicyModule.java"))
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    // Check the effect of setting both module class/field name policies, class name, field
    // name policy and explicit names on fields (i.e = Specific class name + field name should win.
    @Test
    public void compareProcessedNamingPolicyClassFile() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                    JavaFileObjects.forResource("some/test/NamePolicyModule.java"),
                    JavaFileObjects.forResource("some/test/NamePolicyMixedClassSettings.java"),
                    JavaFileObjects.forResource("some/test/NamePolicyFieldNameOnly.java"),
                    JavaFileObjects.forResource("some/test/NamePolicyClassOnly.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("io/realm/some_test_NamePolicyMixedClassSettingsRealmProxy.java"));
    }

    // Check the effect of module default on a class with no settings itself
    @Test
    public void compareProcessedDefaultClassFile() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/NamePolicyModule.java"),
                        JavaFileObjects.forResource("some/test/NamePolicyModuleDefaults.java")
                ))
                .processedWith(new RealmProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("io/realm/some_test_NamePolicyModuleDefaultsRealmProxy.java"));
    }

    // Check that trying to compile two modules with different policies using `allClasses = true` will fail.
    @Test
    public void compileModulesWithConflictingPoliciesForAllClassesFails() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/NamePolicyConflictingModuleDefinitionsForAllClasses.java"),
                        JavaFileObjects.forResource("some/test/Simple.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("disagree on the class naming policy");
    }

    // Check that trying to compile two modules with different policies using `classes = { ... }` will fail.
    @Test
    public void compileModulesWithConflictingPoliciesForNamedClassesFails() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/NamePolicyConflictingModuleDefinitionsForNamedClasses.java"),
                        JavaFileObjects.forResource("some/test/Simple.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("disagree on the class naming policy");
    }

    // Check that trying to compile two modules with different policies using a mix of `allClasses`
    // and `classes = { ... }` will fail.
    @Test
    public void compileModulesWithConflictingPoliciesAndMixedClassDefinitionsFails() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("some/test/NamePolicyConflictingModuleDefinitionsForMixedDefinitions.java"),
                        JavaFileObjects.forResource("some/test/Simple.java")
                ))
                .processedWith(new RealmProcessor())
                .failsToCompile()
                .withErrorContaining("disagree on the class naming policy");
    }

}
