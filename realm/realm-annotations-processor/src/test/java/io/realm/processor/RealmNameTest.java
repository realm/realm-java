package io.realm.processor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Arrays;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.truth0.Truth.ASSERT;

public class RealmNameTest {

    private final JavaFileObject classPolicyOverideModulePolicy = JavaFileObjects.forResource("some/test/NamingPolicyClass.java");
    private final JavaFileObject conflictingModulePolicies = JavaFileObjects.forResource("some/test/NamingPolicyConflictingModules.java");
    private final JavaFileObject simpleModel = JavaFileObjects.forResource("some/test/Simple.java");

    @Test
    public void compileClassPolicyFile() {
        ASSERT.about(javaSource())
                .that(classPolicyOverideModulePolicy)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }

    @Test
    public void conflictingModulePolicyFails() {
        ASSERT.about(javaSources())
                .that(Arrays.asList(conflictingModulePolicies, simpleModel))
                .processedWith(new RealmProcessor())
                .failsToCompile();
    }
}
