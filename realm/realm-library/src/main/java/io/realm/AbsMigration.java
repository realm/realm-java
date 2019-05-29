package io.realm

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 * Created by wlxyhy on 2019/05/29.
 * Extends this to simplify RealmMigration.
 * Add "-keep public class * extends io.realm.RealmObject {*;}" in proguard-rule.pro if minifyEnabled.
 * Do not extends this if there's name-difference between schema fields and your RealmObject fields in your project(eg:field with'@RealmField(name = "person_name")')! 
 */

public abstract class AbsMigration implements RealmMigration {
    /**
     * Classes that extends RealmObject
     *
     * @return Arrays of RealmObject classes that should be used in the related database defined in {@link io.realm.RealmConfiguration},
     * usually set by {@link io.realm.RealmConfiguration.Builder#modules(Object, Object...)},
     * which add new Objects with annotation {@link io.realm.annotations.RealmModule},
     * or all the subclasses of RealmObject in this project if not set.<br>
     * <Strong>Note: if there are references for other RealmObjects in one RealmObject,
     * this RealmObject class must be listed after referenced-RealmObjects!</Strong>
     */
    protected abstract Class<? extends RealmObject>[] getRealmClasses();

    /**
     * If there is any field to rename, use {@link RealmObjectSchema#renameField(String, String)}
     */
    protected abstract void renameFieldIfNeeded(DynamicRealm realm, long oldVersion);

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        renameFieldIfNeeded(realm, oldVersion);
        if (oldVersion < newVersion) {
            attrList = new ArrayList<>();
            RealmSchema schema = realm.getSchema();
            Class<? extends RealmObject>[] classes = getRealmClasses();
            for (Class<? extends RealmObject> aClass : classes) {
                checkProperties(schema, aClass);
            }
        }

    }


    //Check schema and schema fields, create schema if there's no this schema; 
    //not suitable for case when field name change or user-defined field name such as field with'@RealmField(name = "person_name")'
    private void checkProperties(RealmSchema realmSchema, Class<? extends RealmObject> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        String className = clazz.getSimpleName();
        if (!realmSchema.contains(className)) {
            RealmObjectSchema objectSchema = realmSchema.create(className);
            for (Field field : fields) {
                createSchemaField(realmSchema, objectSchema, field);
            }
            return;
        }
        RealmObjectSchema schema = realmSchema.get(className);
        Set<String> fieldNames = schema.getFieldNames();
        for (Field field : fields) {
            if (!fieldNames.remove(field.getName())) {
                createSchemaField(realmSchema, schema, field);
            }
        }
        for (String element : fieldNames) {
            schema.removeField(element);
        }
    }

    private List<FieldAttribute> attrList;

    private void createSchemaField(RealmSchema realmSchema, RealmObjectSchema schema, Field field) {
        if (Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(Ignore.class)) {
            return;
        }
        attrList.clear();
        Class<? extends Annotation>[] cls = new Class[]{Index.class, PrimaryKey.class, Required.class};
        FieldAttribute[] values = FieldAttribute.values();
        for (int i = 0; i < values.length; i++) {
            if (field.isAnnotationPresent(cls[i])) {
                attrList.add(values[i]);
            }
        }
        Class<?> type = field.getType();
        String name = field.getName();
        if (attrList.size() > 0) {//must be class defined by JDK, int, String ...
            schema.addField(name, type, attrList.toArray(new FieldAttribute[0]));
        } else if (type.equals(RealmList.class)) {
            Type[] typeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            String fullName = typeArguments[0].toString();
            String simpleName = fullName.substring(fullName.lastIndexOf(".") + 1);//泛型的类型名
            RealmObjectSchema objSchema = realmSchema.get(simpleName);
            if (objSchema != null) {
                schema.addRealmListField(name, objSchema);
            } else {//other type, such as int,String... not likely to occur
                try {
                    schema.addRealmListField(name, Class.forName(fullName));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (RealmObject.class.isAssignableFrom(type)) {//subclass of RealmObject
            RealmObjectSchema objSchema = realmSchema.get(type.getSimpleName());
            if (objSchema != null) {//make sure the objSchema has been created before, or we should use recursion
                schema.addRealmObjectField(name, objSchema);
            }
        } else {
            schema.addField(name, type);
        }

    }
}
