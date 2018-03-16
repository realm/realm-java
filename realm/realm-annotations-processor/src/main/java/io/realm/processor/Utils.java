package io.realm.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import io.realm.annotations.RealmNamingPolicy;
import io.realm.processor.nameconverter.CamelCaseConverter;
import io.realm.processor.nameconverter.IdentityConverter;
import io.realm.processor.nameconverter.LowerCaseWithSeparatorConverter;
import io.realm.processor.nameconverter.NameConverter;
import io.realm.processor.nameconverter.PascalCaseConverter;

/**
 * Utility methods working with the Realm processor.
 */
public class Utils {

    private static Types typeUtils;
    private static Messager messager;
    private static TypeMirror realmInteger;
    private static DeclaredType realmList;
    private static DeclaredType realmResults;
    private static DeclaredType markerInterface;
    private static TypeMirror realmModel;

    public static void initialize(ProcessingEnvironment env) {
        Elements elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        messager = env.getMessager();
        realmInteger = elementUtils.getTypeElement("io.realm.MutableRealmInteger").asType();
        realmList = typeUtils.getDeclaredType(
                elementUtils.getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null));
        realmResults = typeUtils.getDeclaredType(
                env.getElementUtils().getTypeElement("io.realm.RealmResults"), typeUtils.getWildcardType(null, null));
        realmModel = elementUtils.getTypeElement("io.realm.RealmModel").asType();
        markerInterface = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmModel"));
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

    public static String getProxyClassSimpleName(VariableElement field) {
        if (typeUtils.isAssignable(field.asType(), realmList)) {
            return getProxyClassName(getGenericTypeQualifiedName(field));
        } else {
            return getProxyClassName(getFieldTypeQualifiedName(field));
        }
    }

    /**
     * @return the proxy class name for a given clazz
     */
    public static String getProxyClassName(String qualifiedClassName) {
        return qualifiedClassName.replace(".", "_") + Constants.PROXY_SUFFIX;
    }

    /**
     * @return {@code true} if a field is of type "java.lang.String", {@code false} otherwise.
     * @throws IllegalArgumentException if the field is {@code null}.
     */
    public static boolean isString(VariableElement field) {
        if (field == null) {
            throw new IllegalArgumentException("Argument 'field' cannot be null.");
        }
        return getFieldTypeQualifiedName(field).equals("java.lang.String");
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
        return getFieldTypeQualifiedName(field).equals("byte[]");
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
     * @return {@code true} if a given field type is {@code MutableRealmInteger}, {@code false} otherwise.
     */
    public static boolean isMutableRealmInteger(VariableElement field) {
        return typeUtils.isAssignable(field.asType(), realmInteger);
    }

    /**
     * @return {@code true} if a given field type is {@code RealmList}, {@code false} otherwise.
     */
    public static boolean isRealmList(VariableElement field) {
        return typeUtils.isAssignable(field.asType(), realmList);
    }

    /**
     * @param field {@link VariableElement} of a value list field.
     * @return element type of the list field.
     */
    public static Constants.RealmFieldType getValueListFieldType(VariableElement field) {
        final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
        return Constants.LIST_ELEMENT_TYPE_TO_REALM_TYPES.get(elementTypeMirror.toString());
    }

    /**
     * @return {@code true} if a given field type is {@code RealmList} and its element type is {@code RealmObject},
     * {@code false} otherwise.
     */
    public static boolean isRealmModelList(VariableElement field) {
        final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
        if (elementTypeMirror == null) {
            return false;
        }
        return isRealmModel(elementTypeMirror);
    }

    /**
     * @return {@code true} if a given field type is {@code RealmList} and its element type is value type,
     * {@code false} otherwise.
     */
    public static boolean isRealmValueList(VariableElement field) {
        final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
        if (elementTypeMirror == null) {
            return false;
        }
        return !isRealmModel(elementTypeMirror);
    }

    /**
     * @return {@code true} if a given field type is {@code RealmModel}, {@code false} otherwise.
     */
    public static boolean isRealmModel(Element field) {
        return isRealmModel(field.asType());
    }

    /**
     * @return {@code true} if a given type is {@code RealmModel}, {@code false} otherwise.
     */
    public static boolean isRealmModel(TypeMirror type) {
        // This will return the wrong result if a model class doesn't exist at all, but
        // the compiler will catch that eventually.
        return typeUtils.isAssignable(type, realmModel);
//        // Not sure what is happening here, but typeUtils.isAssignable("Foo", realmModel)
//        // returns true even if Foo doesn't exist. No idea why this is happening.
//        // For now punt on the problem and check the direct supertype which should be either
//        // RealmObject or RealmModel.
//        // Original implementation: ``
//        //
//        // Theory: It looks like if `type` has the internal TypeTag.ERROR (internal API) it
//        // automatically translate to being assignable to everything. Possible some Java Specification
//        // rule taking effect. In our case, however we can do better since all Realm classes
//        // must be in the same compilation unit, so we should be able to look the type up.
//        for (TypeMirror typeMirror : typeUtils.directSupertypes(type)) {
//            String supertype = typeMirror.toString();
//            if (supertype.equals("io.realm.RealmObject") || supertype.equals("io.realm.RealmModel")) {
//                return true;
//            }
//        }
//        return false;
    }

    public static boolean isRealmResults(VariableElement field) {
        return typeUtils.isAssignable(field.asType(), realmResults);
    }

    // get the fully-qualified type name for the generic type of a RealmResults
    public static String getRealmResultsType(VariableElement field) {
        if (!Utils.isRealmResults(field)) { return null; }
        ReferenceType type = getGenericTypeForContainer(field);
        if (null == type) { return null; }
        return type.toString();
    }

    // get the fully-qualified type name for the generic type of a RealmList
    public static String getRealmListType(VariableElement field) {
        if (!Utils.isRealmList(field)) { return null; }
        ReferenceType type = getGenericTypeForContainer(field);
        if (null == type) { return null; }
        return type.toString();
    }

    // Note that, because subclassing subclasses of RealmObject is forbidden,
    // there is no need to deal with constructs like:  <code>RealmResults&lt;? extends Foos&lt;</code>.
    public static ReferenceType getGenericTypeForContainer(VariableElement field) {
        TypeMirror fieldType = field.asType();
        TypeKind kind = fieldType.getKind();
        if (kind != TypeKind.DECLARED) { return null; }

        List<? extends TypeMirror> args = ((DeclaredType) fieldType).getTypeArguments();
        if (args.size() <= 0) { return null; }

        fieldType = args.get(0);
        kind = fieldType.getKind();
        // We also support RealmList<byte[]>
        if (kind != TypeKind.DECLARED && kind != TypeKind.ARRAY) { return null; }

        return (ReferenceType) fieldType;
    }

    /**
     * @return the qualified type name for a field.
     */
    public static String getFieldTypeQualifiedName(VariableElement field) {
        return field.asType().toString();
    }

    /**
     * @return the generic type for Lists of the form {@code List<type>}
     */
    public static String getGenericTypeQualifiedName(VariableElement field) {
        TypeMirror fieldType = field.asType();
        List<? extends TypeMirror> typeArguments = ((DeclaredType) fieldType).getTypeArguments();
        if (typeArguments.size() == 0) {
            return null;
        }
        return typeArguments.get(0).toString();
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
        if (element instanceof RealmFieldElement) {
            // Element is being cast to Symbol internally which breaks any implementors of the
            // Element interface. This is a hack to work around that. Bad bad Oracle
            element = ((RealmFieldElement) element).getFieldReference();
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    public static void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    public static void note(String message, Element element) {
        if (element instanceof RealmFieldElement) {
            // Element is being cast to Symbol internally which breaks any implementors of the
            // Element interface. This is a hack to work around that. Bad bad Oracle
            element = ((RealmFieldElement) element).getFieldReference();
        }
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    public static void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    public static Element getSuperClass(TypeElement classType) {
        return typeUtils.asElement(classType.getSuperclass());
    }

    /**
     * Returns the interface name for proxy class interfaces
     */
    public static String getProxyInterfaceName(String qualifiedClassName) {
        return qualifiedClassName.replace(".", "_") + Constants.INTERFACE_SUFFIX;
    }

    public static NameConverter getNameFormatter(RealmNamingPolicy policy) {
        if (policy == null) {
            return new IdentityConverter();
        }
        switch (policy) {
            case NO_POLICY: return new IdentityConverter();
            case IDENTITY: return new IdentityConverter();
            case LOWER_CASE_WITH_UNDERSCORES: return new LowerCaseWithSeparatorConverter('_');
            case CAMEL_CASE: return new CamelCaseConverter();
            case PASCAL_CASE: return new PascalCaseConverter();
            default:
                throw new IllegalArgumentException("Unknown policy: " + policy);
        }
    }

    /**
     * Tries to find the internal class name for a referenced type. In model classes this can
     * happen with either direct object references or using `RealmList` or `RealmResults`.
     * <p>
     * This name is required by schema builders that operate on internal names and not the public ones.
     * <p>
     * Finding the internal name is easy if the referenced type is included in the current round
     * of annotation processing. In that case the internal name was also calculated in the same round
     * <p>
     * If the referenced type was already compiled, e.g being included from library, then we need
     * to get the name from the proxy class. Fortunately ProGuard should not have obfuscated any
     * class files at this point, meaning we can look it up dynamically.
     * <p>
     * If a name is looked up using the class loader, it also means that developers need to
     * combine a library and app module of model classes at runtime in the RealmConfiguration, but
     * this should be a valid use case.
     *
     * @param qualifiedClassName type to lookup the internal name for.
     * @param classCollection collection of classes found in the current round of annotation processing.
     * @throws IllegalArgumentException If the internal name could not be looked up
     * @return the statement that evalutes to the internal class name. This will either be a string
     * constant or a reference to a static field in another class. In both cases, the return result
     * should not be put in quotes.
     */
    public static String getReferencedTypeInternalClassNameStatement(String qualifiedClassName, ClassCollection classCollection) {

        // Attempt to lookup internal name in current round
        if (classCollection.containsQualifiedClass(qualifiedClassName)) {
            ClassMetaData metadata = classCollection.getClassFromQualifiedName(qualifiedClassName);
            return "\"" + metadata.getInternalClassName() + "\"";
        }

        // If we cannot find the name in the current processor round, we have to defer resolving the
        // name to runtime. The reason being that the annotation processor can only access the
        // compile type class path using Elements and Types which do not allow us to read
        // field values.
        //
        // Doing it this way unfortunately means that if the class is not on the apps classpath
        // a rather obscure class-not-found exception will be thrown when starting the app, but since
        // this is probably a very niche use case that is acceptable for now.
        //
        // TODO: We could probably create an internal annotation like `@InternalName("__Permission")`
        // which should make it possible for the annotation processor to read the value from the
        // proxy class, even for files in other jar files.
        return "io.realm." + Utils.getProxyClassName(qualifiedClassName) + ".ClassNameHelper.INTERNAL_CLASS_NAME";
    }
}
