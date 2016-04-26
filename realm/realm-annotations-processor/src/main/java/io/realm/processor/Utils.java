package io.realm.processor;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Utility methods working with the Realm processor.
 */
public class Utils {

    public static Types typeUtils;
    private static Messager messager;
    private static DeclaredType realmList;
    private static DeclaredType markerInterface;
    private static TypeMirror realmModel;

    public static void initialize(ProcessingEnvironment env) {
        typeUtils = env.getTypeUtils();
        messager = env.getMessager();
        realmList = typeUtils.getDeclaredType(env.getElementUtils().getTypeElement("io.realm.RealmList"),
                typeUtils.getWildcardType(null, null));
        realmModel = env.getElementUtils().getTypeElement("io.realm.RealmModel").asType();
        markerInterface = env.getTypeUtils().getDeclaredType(env.getElementUtils().getTypeElement("io.realm.RealmModel"));
    }

    /**
     * @return true if the given element is the default public no arg constructor for a class.
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
     * @return the proxy class name for a given clazz
     */
    public static String getProxyClassName(String clazz) {
        return clazz + Constants.PROXY_SUFFIX;
    }

    /**
     * @return {@code true} if a field is of type "java.lang.String", {@code false} otherwise.
     * @throws IllegalArgumentException if the field is {@code null}.
     */
    public static boolean isString(VariableElement field) {
        if (field == null) {
            throw new IllegalArgumentException("Argument 'field' cannot be null.");
        }
        return getFieldTypeSimpleName(field).equals("String");
    }

    /**
     * @return {@code true} if a field is a primitive type, {@code false} otherwise.
     * @throws IllegalArgumentException if the typeString is {@code null}.
     */
    public static boolean isPrimitiveType(String typeString) {
        if (typeString == null) {
            throw new IllegalArgumentException("Argument 'typeString' cannot be null.");
        }
        return typeString.equals("byte") || typeString.equals("short") || typeString.equals("int") ||
                typeString.equals("long") || typeString.equals("float") || typeString.equals("double") ||
                typeString.equals("boolean") || typeString.equals("char");
    }

    /**
     * @return {@code true} if a field is a boxed type, {@code false} otherwise.
     * @throws IllegalArgumentException if the typeString is {@code null}.
     */
    public static boolean isBoxedType(String typeString) {
        if (typeString == null) {
            throw new IllegalArgumentException("Argument 'typeString' cannot be null.");
        }
        return typeString.equals(Byte.class.getName()) || typeString.equals(Short.class.getName()) ||
                typeString.equals(Integer.class.getName()) || typeString.equals(Long.class.getName()) ||
                typeString.equals(Float.class.getName()) || typeString.equals(Double.class.getName()) ||
                typeString.equals(Boolean.class.getName());
    }

    /**
     * @return {@code true} if a field is a type of primitive types, {@code false} otherwise.
     * @throws IllegalArgumentException if the field is {@code null}.
     */
    public static boolean isPrimitiveType(VariableElement field) {
        if (field == null) {
            throw new IllegalArgumentException("Argument 'field' cannot be null.");
        }
        return field.asType().getKind().isPrimitive();
    }

    /**
     * @return {@code true} if a field is of type "byte[]", {@code false} otherwise.
     * @throws IllegalArgumentException if the field is {@code null}.
     */
    public static boolean isByteArray(VariableElement field) {
        if (field == null) {
            throw new IllegalArgumentException("Argument 'field' cannot be null.");
        }
        return getFieldTypeSimpleName(field).equals("byte[]");
    }

    /**
     * @return {@code true} if a given field type string is "java.lang.String", {@code false} otherwise.
     * @throws IllegalArgumentException if the fieldType is {@code null}.
     */
    public static boolean isString(String fieldType) {
        if (fieldType == null) {
            throw new IllegalArgumentException("Argument 'fieldType' cannot be null.");
        }
        return String.class.getName().equals(fieldType);
    }

    /**
     * @return {@code true} if a given type implement {@code RealmModel}, {@code false} otherwise.
     */
    public static boolean isImplementingMarkerInterface(Element classElement) {
        return typeUtils.isAssignable(classElement.asType(), markerInterface);
    }

    /**
     * @return {@code true} if a given field type is {@code RealmList}, {@code false} otherwise.
     */
    public static boolean isRealmList(VariableElement field) {
        return typeUtils.isAssignable(field.asType(), realmList);
    }

    /**
     * @return {@code true} if a given field type is {@code RealmModel}, {@code false} otherwise.
     */
    public static boolean isRealmModel(VariableElement field) {
        return typeUtils.isAssignable(field.asType(), realmModel);
    }


    /**
     * @return the simple type name for a field.
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
     * @return the generic type for Lists of the form {@code List<type>}
     */
    public static String getGenericType(VariableElement field) {
        TypeMirror fieldType = field.asType();
        List<? extends TypeMirror> typeArguments = ((DeclaredType) fieldType).getTypeArguments();
        if (typeArguments.size() == 0) {
            return null;
        }
        String genericCanonicalType = (String) typeArguments.get(0).toString();
        String genericType;
        if (genericCanonicalType.contains(".")) {
            genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
        } else {
            genericType = genericCanonicalType;
        }
        return genericType;
    }

    /**
     * Strips the package name from a fully qualified class name.
     */
    public static String stripPackage(String fullyQualifiedClassName) {
        String[] parts = fullyQualifiedClassName.split("\\.");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        } else {
            return fullyQualifiedClassName;
        }
    }

    public static void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    public static void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    public static void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    public static Element getSuperClass(TypeElement classType) {
        return typeUtils.asElement(classType.getSuperclass());
    }

    public static String getProxyInterfaceName(String className) {
        return className + Constants.INTERFACE_SUFFIX;
    }
}
