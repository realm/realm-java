package io.realm.processor;

import java.util.List;
import java.util.ListIterator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Utility methods working with the Realm processor.
 */
public class Utils {

    public static Types typeUtils;
    private static DeclaredType realmList;

    public static void initialize(ProcessingEnvironment env) {
        typeUtils = env.getTypeUtils();
        realmList = typeUtils.getDeclaredType(env.getElementUtils().getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null));
    }

    /**
     * Checks if a given type is either on a list of valid types or is a subclass of such a type
     */
    public static boolean isValidType(Types typeUtils, TypeMirror type, List<TypeMirror> validTypes) {
        for (TypeMirror validType : validTypes) {
            if (typeUtils.isAssignable(type, validType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given element is the default public no arg constructor for a class.
     */
    public static boolean isDefaultConstructor(Element constructor) {
        if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
            return ((ExecutableElement) constructor).getParameters().isEmpty();
        }
        return false;
    }

    public static String lowerFirstChar(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public static String getProxyClassSimpleName(VariableElement field) {
        if (typeUtils.isAssignable(field.asType(), realmList)) {
            return getProxyClassName(getGenericType(field));
        } else {
            return getProxyClassName(getFieldTypeSimpleName(field));
        }
    }

    /**
     * Return the proxy class name for a given clazz
     */
    public static String getProxyClassName(String clazz) {
        return clazz + RealmProxyClassGenerator.PROXY_SUFFIX;
    }

    /**
     * Returns the simple name of a class by stripping its package name.
     */
    public static String stripPackage(String clazz) {
        String[] parts = clazz.split("\\.");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        } else {
            return clazz;
        }
    }

    public static String joinStringList(List<String> strings, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext()) {
            int index = iterator.nextIndex();
            String item = iterator.next();

            if (index > 0) {
                stringBuilder.append(separator);
            }
            stringBuilder.append(item);
        }
        return stringBuilder.toString();
    }

    /**
     * Returns true if a field is of type "java.lang.String", false otherwise.
     */
    public static boolean isString(VariableElement field) {
        if (field == null) {
            return false;
        }
        return getFieldTypeSimpleName(field).equals("String");
    }

    /**
     * Returns the simple type name for a field.
     */
    public static String getFieldTypeSimpleName(VariableElement field) {
        String fieldTypeCanonicalName = field.asType().toString();
        String fieldTypeName;
        if (fieldTypeCanonicalName.contains(".")) {
            fieldTypeName = fieldTypeCanonicalName.substring(fieldTypeCanonicalName.lastIndexOf('.') + 1);
        } else {
            fieldTypeName = fieldTypeCanonicalName;
        }
        return fieldTypeName;
    }

    /**
     * Returns the generic type for Lists of the form {@code List<type>}
     */
    public static String getGenericType(VariableElement field) {
        String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
        String genericType;
        if (genericCanonicalType.contains(".")) {
            genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
        } else {
            genericType = genericCanonicalType;
        }
        return genericType;
    }
}
