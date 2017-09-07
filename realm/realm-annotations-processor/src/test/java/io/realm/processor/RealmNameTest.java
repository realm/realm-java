package io.realm.processor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class RealmNameTest {

    private final JavaFileObject classPolicyOverideModulePolicy = JavaFileObjects.forResource("some/test/RealmNameClassPolicy.java");

    @Test
    public void compileClassPolicyFile() {
        ASSERT.about(javaSource())
                .that(classPolicyOverideModulePolicy)
                .processedWith(new RealmProcessor())
                .compilesWithoutError();
    }
}
