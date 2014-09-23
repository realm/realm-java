package io.realm.internal;

import android.annotation.TargetApi;
import android.os.Build;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class JNITestSuite extends TestSuite {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public JNITestSuite(
            final Class<? extends TestCase> testCaseClass,
            final Collection<Object[]> parameters) {

        setName(testCaseClass.getName());

        final Constructor<?>[] constructors = testCaseClass.getConstructors();
        if (constructors.length != 1) {
            addTest(warning(testCaseClass.getName()
                    + " must have a single public constructor."));
            return;
        }

        final Collection<String> names = getTestMethods(testCaseClass);

        final Constructor<?> constructor = constructors[0];
        final Collection<TestCase> testCaseInstances = new ArrayList<TestCase>();
        try {
            for (final Object[] objects : parameters) {
                for (final String name : names) {
                    TestCase testCase = (TestCase) constructor.newInstance(objects);
                    testCase.setName(name);
                    testCaseInstances.add(testCase);
                }
            }
        } catch (IllegalArgumentException e) {
            addConstructionException(e);
            return;
        } catch (InstantiationException e) {
            addConstructionException(e);
            return;
        } catch (IllegalAccessException e) {
            addConstructionException(e);
            return;
        } catch (InvocationTargetException e) {
            addConstructionException(e);
            return;
        }


        for (final TestCase testCase : testCaseInstances) {
            addTest(testCase);

        }
    }

    private Collection<String> getTestMethods(
            final Class<? extends TestCase> testCaseClass) {
        Class<?> superClass = testCaseClass;
        final Collection<String> names = new ArrayList<String>();
        while (Test.class.isAssignableFrom(superClass)) {
            Method[] methods = superClass.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                addTestMethod(methods[i], names, testCaseClass);
            }
            superClass = superClass.getSuperclass();
        }
        return names;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addTestMethod(Method m, Collection<String> names, Class<?> theClass) {
        String name = m.getName();
        if (names.contains(name))
            return;
        if (!isPublicTestMethod(m)) {
            if (isTestMethod(m))
                addTest(warning("Test method isn't public: " + m.getName()));
            return;
        }
        names.add(name);
    }

    private boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

    private boolean isTestMethod(Method m) {
        String name = m.getName();
        Class<?>[] parameters = m.getParameterTypes();
        Class<?> returnType = m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addConstructionException(Exception e) {
        addTest(warning("Instantiation of a testCase failed "
                + e.getClass().getName() + " " + e.getMessage()));
    }

}
