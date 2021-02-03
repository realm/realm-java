/*
 * Copyright 2019 Realm Inc.
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
 
package io.realm.processor

import io.realm.annotations.RealmNamingPolicy
import io.realm.processor.nameconverter.*
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ReferenceType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * Utility methods working with the Realm processor.
 */
object Utils {

    private lateinit var typeUtils: Types
    private lateinit var messager: Messager
    private lateinit var realmInteger: TypeMirror
    private lateinit var mixed: TypeMirror
    private lateinit var realmList: DeclaredType
    private lateinit var realmResults: DeclaredType
    private lateinit var markerInterface: DeclaredType
    private lateinit var realmModel: TypeMirror
    private lateinit var realmDictionary: DeclaredType

    fun initialize(env: ProcessingEnvironment) {
        val elementUtils = env.elementUtils
        typeUtils = env.typeUtils
        messager = env.messager
        realmInteger = elementUtils.getTypeElement("io.realm.MutableRealmInteger").asType()
        mixed = elementUtils.getTypeElement("io.realm.Mixed").asType()
        realmList = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null))
        realmResults = typeUtils.getDeclaredType(env.elementUtils.getTypeElement("io.realm.RealmResults"), typeUtils.getWildcardType(null, null))
        realmModel = elementUtils.getTypeElement("io.realm.RealmModel").asType()
        markerInterface = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmModel"))
        realmDictionary = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmDictionary"), typeUtils.getWildcardType(null, null))
    }

    /**
     * @return true if the given element is the default public no arg constructor for a class.
     */
    fun isDefaultConstructor(constructor: Element): Boolean {
        return if (constructor.modifiers.contains(Modifier.PUBLIC)) {
            (constructor as ExecutableElement).parameters.isEmpty()
        } else false
    }

    fun getProxyClassSimpleName(field: VariableElement): SimpleClassName {
        return if (typeUtils.isAssignable(field.asType(), realmList)) {
            getProxyClassName(getGenericTypeQualifiedName(field)!!)
        } else {
            getProxyClassName(getFieldTypeQualifiedName(field))
        }
    }

    fun getDictionaryGenericProxyClassSimpleName(field: VariableElement): SimpleClassName {
        return if (typeUtils.isAssignable(field.asType(), realmDictionary)) {
            getProxyClassName(getGenericTypeQualifiedName(field)!!)
        } else {
            getProxyClassName(getFieldTypeQualifiedName(field))
        }
    }

    fun getModelClassQualifiedName(field: VariableElement): QualifiedClassName {
        return if (typeUtils.isAssignable(field.asType(), realmList)) {
            getGenericTypeQualifiedName(field)!!
        } else {
            getFieldTypeQualifiedName(field)
        }
    }

    fun getDictionaryGenericModelClassQualifiedName(field: VariableElement): QualifiedClassName {
        return if (typeUtils.isAssignable(field.asType(), realmDictionary)) {
            getGenericTypeQualifiedName(field)!!
        } else {
            getFieldTypeQualifiedName(field)
        }
    }

    /**
     * @return the proxy class name for a given clazz
     */
    fun getProxyClassName(className: QualifiedClassName): SimpleClassName {
        return SimpleClassName(className.toString().replace(".", "_") + Constants.PROXY_SUFFIX)
    }

    /**
     * @return `true` if a field is of type "java.lang.String", `false` otherwise.
     * @throws IllegalArgumentException if the field is `null`.
     */
    fun isString(field: VariableElement?): Boolean {
        if (field == null) {
            throw IllegalArgumentException("Argument 'field' cannot be null.")
        }
        return getFieldTypeQualifiedName(field).toString() == "java.lang.String"
    }

    /**
     * @return `true` if a field is of type "org.bson.types.ObjectId", `false` otherwise.
     * @throws IllegalArgumentException if the field is `null`.
     */
    fun isObjectId(field: VariableElement?): Boolean {
        if (field == null) {
            throw IllegalArgumentException("Argument 'field' cannot be null.")
        }
        return getFieldTypeQualifiedName(field).toString() == "org.bson.types.ObjectId"
    }

    /**
     * @return `true` if a field is of type "java.util.UUID", `false` otherwise.
     * @throws IllegalArgumentException if the field is `null`.
     */
    fun isUUID(field: VariableElement?): Boolean {
        if (field == null) {
            throw IllegalArgumentException("Argument 'field' cannot be null.")
        }
        return getFieldTypeQualifiedName(field).toString() == "java.util.UUID"
    }

    /**
     * @return `true` if a field is a primitive type, `false` otherwise.
     * @throws IllegalArgumentException if the typeString is `null`.
     */
    fun isPrimitiveType(typeString: String): Boolean {
        return typeString == "byte" || typeString == "short" || typeString == "int" ||
                typeString == "long" || typeString == "float" || typeString == "double" ||
                typeString == "boolean" || typeString == "char"
    }

    fun isPrimitiveType(type: QualifiedClassName): Boolean {
        return isPrimitiveType(type.toString())
    }

    /**
     * @return `true` if a field is a boxed type, `false` otherwise.
     * @throws IllegalArgumentException if the typeString is `null`.
     */
    fun isBoxedType(typeString: String?): Boolean {
        if (typeString == null) {
            throw IllegalArgumentException("Argument 'typeString' cannot be null.")
        }
        return typeString == Byte::class.javaObjectType.name || typeString == Short::class.javaObjectType.name ||
                typeString == Int::class.javaObjectType.name || typeString == Long::class.javaObjectType.name ||
                typeString == Float::class.javaObjectType.name || typeString == Double::class.javaObjectType.name ||
                typeString == Boolean::class.javaObjectType.name
    }

    /**
     * @return `true` if a field is a type of primitive types, `false` otherwise.
     * @throws IllegalArgumentException if the field is `null`.
     */
    fun isPrimitiveType(field: VariableElement?): Boolean {
        if (field == null) {
            throw IllegalArgumentException("Argument 'field' cannot be null.")
        }
        return field.asType().kind.isPrimitive
    }

    /**
     * @return `true` if a field is of type "byte[]", `false` otherwise.
     * @throws IllegalArgumentException if the field is `null`.
     */
    fun isByteArray(field: VariableElement?): Boolean {
        if (field == null) {
            throw IllegalArgumentException("Argument 'field' cannot be null.")
        }
        return getFieldTypeQualifiedName(field).toString() == "byte[]"
    }

    /**
     * @return `true` if a given field type string is "java.lang.String", `false` otherwise.
     * @throws IllegalArgumentException if the fieldType is `null`.
     */
    fun isString(fieldType: String?): Boolean {
        if (fieldType == null) {
            throw IllegalArgumentException("Argument 'fieldType' cannot be null.")
        }
        return String::class.java.name == fieldType
    }

    /**
     * @return `true` if a given type implement `RealmModel`, `false` otherwise.
     */
    fun isImplementingMarkerInterface(classElement: Element): Boolean {
        return typeUtils.isAssignable(classElement.asType(), markerInterface)
    }

    /**
     * @return `true` if a given field type is `MutableRealmInteger`, `false` otherwise.
     */
    fun isMutableRealmInteger(field: VariableElement): Boolean {
        return typeUtils.isAssignable(field.asType(), realmInteger)
    }

    /**
     * @return `true` if a given field type is `Mixed`, `false` otherwise.
     */
    fun isMixed(field: VariableElement): Boolean {
        return typeUtils.isAssignable(field.asType(), mixed)
    }

    /**
     * @return `true` if a given field type is `RealmList`, `false` otherwise.
     */
    fun isRealmList(field: VariableElement): Boolean {
        return typeUtils.isAssignable(field.asType(), realmList)
    }

    /**
     * @return `true` if a given field type is `RealmDictionary`, `false` otherwise.
     */
    fun isRealmDictionary(field: VariableElement): Boolean {
        return typeUtils.isAssignable(field.asType(), realmDictionary)
    }

    /**
     * @return `true` if a given field type is `RealmDictionary<RealmModel>`, `false` otherwise.
     */
    fun isRealmModelDictionary(field: VariableElement): Boolean {
        val elementTypeMirror = TypeMirrors.getRealmDictionaryElementTypeMirror(field) ?: return false
        return isRealmModel(elementTypeMirror)
    }

    /**
     * @param field [VariableElement] of a value list field.
     * @return element type of the list field.
     */
    fun getValueListFieldType(field: VariableElement): Constants.RealmFieldType {
        val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field)
        return Constants.LIST_ELEMENT_TYPE_TO_REALM_TYPES[elementTypeMirror!!.toString()]
                ?: throw IllegalArgumentException("Invalid type mirror '$elementTypeMirror' for field '$field'")
    }

    /**
     * @param field [VariableElement] of a value dictionary field.
     * @return element type of the dictionary field.
     */
    fun getValueDictionaryFieldType(field: VariableElement): Constants.RealmFieldType {
        val elementTypeMirror = TypeMirrors.getRealmDictionaryElementTypeMirror(field)
        return Constants.DICTIONARY_ELEMENT_TYPE_TO_REALM_TYPES[elementTypeMirror!!.toString()]
                ?: throw IllegalArgumentException("Invalid type mirror '$elementTypeMirror' for field '$field'")
    }

    /**
     * @return `true` if a given field type is `RealmList` and its element type is `RealmObject`,
     * `false` otherwise.
     */
    fun isRealmModelList(field: VariableElement): Boolean {
        val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field) ?: return false
        return isRealmModel(elementTypeMirror)
    }

    /**
     * @return `true` if a given field type is `RealmList` and its element type is value type,
     * `false` otherwise.
     */
    fun isRealmValueList(field: VariableElement): Boolean {
        val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field) ?: return false
        return !isRealmModel(elementTypeMirror)
    }

    /**
     * @return `true` if a given field type is `RealmModel`, `false` otherwise.
     */
    fun isRealmModel(field: Element): Boolean {
        return isRealmModel(field.asType())
    }

    /**
     * @return `true` if a given type is `RealmModel`, `false` otherwise.
     */
    fun isRealmModel(type: TypeMirror?): Boolean {
        // This will return the wrong result if a model class doesn't exist at all, but
        // the compiler will catch that eventually.
        return typeUtils.isAssignable(type, realmModel)
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

    /**
     * @return `true` if a given type is `Mixed`, `false` otherwise.
     */
    fun isMixed(type: TypeMirror?) = typeUtils.isAssignable(type, mixed)

    fun isRealmResults(field: VariableElement): Boolean {
        return typeUtils.isAssignable(field.asType(), realmResults)
    }

    // get the fully-qualified type name for the generic type of a RealmResults
    fun getRealmResultsType(field: VariableElement): QualifiedClassName? {
        if (!isRealmResults(field)) {
            return null
        }
        val type = getGenericTypeForContainer(field) ?: return null
        return QualifiedClassName(type.toString())
    }

    // get the fully-qualified type name for the generic type of a RealmList
    fun getRealmListType(field: VariableElement): QualifiedClassName? {
        if (!isRealmList(field)) {
            return null
        }
        val type = getGenericTypeForContainer(field) ?: return null
        return QualifiedClassName(type.toString())
    }

    fun getDictionaryType(field: VariableElement): QualifiedClassName? {
        if (!isRealmDictionary(field)) {
            return null
        }
        val type = getGenericTypeForContainer(field) ?: return null
        return QualifiedClassName(type.toString())
    }

    // Note that, because subclassing subclasses of RealmObject is forbidden,
    // there is no need to deal with constructs like:  <code>RealmResults&lt;? extends Foos&lt;</code>.
    fun getGenericTypeForContainer(field: VariableElement): ReferenceType? {
        var fieldType = field.asType()
        var kind = fieldType.kind
        if (kind != TypeKind.DECLARED) {
            return null
        }

        val args = (fieldType as DeclaredType).typeArguments
        if (args.size <= 0) {
            return null
        }

        fieldType = args[0]
        kind = fieldType.kind
        // We also support RealmList<byte[]>
        return if (kind != TypeKind.DECLARED && kind != TypeKind.ARRAY) {
            null
        } else fieldType as ReferenceType

    }

    /**
     * @return the qualified type name for a field.
     */
    fun getFieldTypeQualifiedName(field: VariableElement): QualifiedClassName {
        return QualifiedClassName(field.asType().toString())
    }

    /**
     * @return the generic type for Lists of the form `List<type>`
     */
    fun getGenericTypeQualifiedName(field: VariableElement): QualifiedClassName? {
        val fieldType = field.asType()
        val typeArguments = (fieldType as DeclaredType).typeArguments
        return if (typeArguments.isEmpty()) null else QualifiedClassName(typeArguments[0].toString())
    }

    /**
     * @return the generic type for Dictionaries of the form `RealmDictionary<type>`
     * Note: it applies to same types as RealmList.
     */
    fun getDictionaryValueTypeQualifiedName(field: VariableElement): QualifiedClassName? {
        return getGenericTypeQualifiedName(field)
    }

    /**
     * Return generic type mirror if any.
     */
    fun getGenericType(field: VariableElement): TypeMirror? {
        val fieldType = field.asType()
        val typeArguments = (fieldType as DeclaredType).typeArguments
        return if (typeArguments.isEmpty()) null else typeArguments[0]
    }

    /**
     * Strips the package name from a fully qualified class name.
     */
    fun stripPackage(fullyQualifiedClassName: String): String {
        val parts = fullyQualifiedClassName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.isNotEmpty()) {
            parts[parts.size - 1]
        } else {
            fullyQualifiedClassName
        }
    }

    fun error(message: String?, element: Element) {
        var e = element
        if (element is RealmFieldElement) {
            // Element is being cast to Symbol internally which breaks any implementors of the
            // Element interface. This is a hack to work around that. Bad bad Oracle
            e = element.fieldReference
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, e)
    }

    fun error(message: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    fun note(message: String?, element: Element) {
        var e = element
        if (element is RealmFieldElement) {
            // Element is being cast to Symbol internally which breaks any implementors of the
            // Element interface. This is a hack to work around that. Bad bad Oracle
            e = element.fieldReference
        }
        messager.printMessage(Diagnostic.Kind.NOTE, message, e)
    }

    fun note(message: String?) {
        messager.printMessage(Diagnostic.Kind.NOTE, message)
    }

    fun getSuperClass(classType: TypeElement): Element {
        return typeUtils.asElement(classType.superclass)
    }

    /**
     * Returns the interface name for proxy class interfaces
     */
    fun getProxyInterfaceName(qualifiedClassName: QualifiedClassName): SimpleClassName {
        return SimpleClassName(qualifiedClassName.toString().replace(".", "_") + Constants.INTERFACE_SUFFIX)
    }

    fun getNameFormatter(policy: RealmNamingPolicy?): NameConverter {
        if (policy == null) {
            return IdentityConverter()
        }
        when (policy) {
            RealmNamingPolicy.NO_POLICY -> return IdentityConverter()
            RealmNamingPolicy.IDENTITY -> return IdentityConverter()
            RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES -> return LowerCaseWithSeparatorConverter('_')
            RealmNamingPolicy.CAMEL_CASE -> return CamelCaseConverter()
            RealmNamingPolicy.PASCAL_CASE -> return PascalCaseConverter()
            else -> throw IllegalArgumentException("Unknown policy: $policy")
        }
    }

    /**
     * Tries to find the internal class name for a referenced type. In model classes this can
     * happen with either direct object references or using `RealmList` or `RealmResults`.
     *
     *
     * This name is required by schema builders that operate on internal names and not the public ones.
     *
     *
     * Finding the internal name is easy if the referenced type is included in the current round
     * of annotation processing. In that case the internal name was also calculated in the same round
     *
     *
     * If the referenced type was already compiled, e.g being included from library, then we need
     * to get the name from the proxy class. Fortunately ProGuard should not have obfuscated any
     * class files at this point, meaning we can look it up dynamically.
     *
     *
     * If a name is looked up using the class loader, it also means that developers need to
     * combine a library and app module of model classes at runtime in the RealmConfiguration, but
     * this should be a valid use case.
     *
     * @param className type to lookup the internal name for.
     * @param classCollection collection of classes found in the current round of annotation processing.
     * @throws IllegalArgumentException If the internal name could not be looked up
     * @return the statement that evalutes to the internal class name. This will either be a string
     * constant or a reference to a static field in another class. In both cases, the return result
     * should not be put in quotes.
     */
    fun getReferencedTypeInternalClassNameStatement(className: QualifiedClassName?, classCollection: ClassCollection): String {

        // Attempt to lookup internal name in current round
        if (classCollection.containsQualifiedClass(className)) {
            val metadata = classCollection.getClassFromQualifiedName(className!!)
            return "\"" + metadata.internalClassName + "\""
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
        return "io.realm.${getProxyClassName(className!!)}.ClassNameHelper.INTERNAL_CLASS_NAME"
    }

    /**
     * Returns a simple reference to the ColumnInfo class inside this model class, i.e. the package
     * name is not prefixed.
     */
    fun getSimpleColumnInfoClassName(className: QualifiedClassName): String {
        val simpleModelClassName = className.getSimpleName()
        return "${getProxyClassName(className)}.${simpleModelClassName}ColumnInfo"
    }
}
