# Dynamic / Migration API

This contains a description of how a combined Dynamic/Migration could look like:


*Design goals

- Should support stepwise migration.

- Introduce as few new classes as possible.

- Keep terminology as close to familiar concepts as possible.


*Secondary design goals

- Migrations should just feel like switching to dynamic mode.



* Why *

Currently Cococa uses automatic migrations. This concept is great at first, as
it tries to hide the schema concept as much as possible, but it also has a lot
of maintainability issues. The general consensus is we should migrate to
stepwise migration, ie. specify all changes between each version.

This will increase the burden on the developer for simple changes like adding
new classes or fields, but it is code that can be written once and then
forgotten. Automatic migration code has to be maintained always.

At least Android is also going to create a IntelliJ plugin for the Realm
Browser. Adding functionality for automatically creating migration code that can
be pasted in would be fairly straight forward.


* New API *

To support the above goals I suggest adding the following new classes:

DynamicRealm       // Copy of Realm interfaces to allow dynamic access
DynamicRealmObject // Wrapper for all current model classes

RealmSchema        // Object for controlling Realm-Core tables 
RealmObjectSchema // Object for controlling Realm-Core columns

RealmList, RealmQuery and RealmResults can be reused as they are, which minimizes the number of new classes.

## Examples

```
// Dynamic/Typed is chosen when opening the Realm
// Users should not be able to switch once it is open. Is there any use case for that?

RealmConfiguration config = new RealmConfiguration.Builder(context).build();
RealmConfiguration dynamicConfig = new RealmConfiguration.Builder(context).build();

Realm.setDefaultConfiguration(config);
DynamicRealm.setDefaultInstance(dynamic);

Realm realm = Realm.getDefaultInstance(); // Typed version
DynamicRealm dynamicRealm = DynamicRealm.getDefaultInstance() // Dynamic version

// Creating objects
DynamicRealmObject obj = dynamicRealm.createObject("Dog"); // Dog is name of typed class
obj.setString("name", "fido");
obj.setInt("age", 42);

// Queries
RealmResults<DynamicRealmObject> results = dynamicRealm.where("Dog").equalsTo("name", "fido").findAll();
for (DynamicRealmObject obj : results) {
    obj.getString("name");
    obj.getInt("age");
}

RealmQuery<DynamicRealmQuery> query = new RealmQuery<DynamicRealmObject>(dynamicRealm, "Dog").equalTo("name", "Fido");
DynamicRealmObjet dog = query.findFirst();


// DynamicRealm allows schema access
RealmSchema schema = dynamicRealm.getSchema();
schema.addClass("Cat");

// Migrations now just become a special case of using the dynamic Realm
// The DynamicRealm is used to query, create, delete objects
// The Schema is used to manipulate tables/columns
// Schema.forEach() is the odd duck out as it allows you to iterate elements, but it 
// makes it more fluent.

new MyMigration implements Migration {
  @Override
  public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
    RealmSchema = realm.getSchema(); // Annyoing to do this all the time?

    if (oldVersion == 0) {

        schema.getClass("Person")
                .addStringField("fullName")
                .forEach(new DynamicRealm.Iterator() {
                    @Override
                    public void next(DynamicRealmObject obj) {
                        obj.setString("fullName", obj.getString("firstName") + " " + obj.getString("lastName"));
                    }
                })
                .removeField("firstName")
                .removeField("lastName");

        DynamicRealmObjet p = realm.createObject("Person");
        p.setString("name", "John");        
        p.setInt("age", 42);
         
        oldVersion++;
    }

    if (oldVersion == 1) {

        schema.addClass("Cat")
            .addStringField("name")
            .addIntField("age")
            .setObject("owner", schema.getClass("Person"));

        schema.getClass("Person")
            .addIndex("fullName")   

        oldVersion++;
    }
  }
}
```


## Naming

We have two new concepts. Right now we are calling them Dynamic And Schema, is
that accidential or is it the names we want for it?

* Suggestions for 1*
Dynamic 
Untyped

* Suggestions for 2*
Schema
Spec
Definition

Some arguments already brough up:

- Dynamic sounds better than a normal Realm -> Developers might try to start there instead?

- Schema : Everybody hates schemas, but at the same time everybody also knows what the word means.

- Spec is already used by other Java libraries/framework classes to describe class files.



## Current action points

- Is this an acceptable approach?
- Determine naming of concepts
- Investigate impact of removing RealmObject on this suggestion


## Below is the complete list of interfaces for these methods


*DynamicRealm*

// Dynamic Realm is a "smaller" version of the Realm API. Most static methods have been removed. All JSON methods are not available.
public class DynamicRealm {

    // Static methods
    static DynamicRealm getInstance(RealmConfiguration config);

    // Does migrating a Dynamic Realm makes sense?
    static void migrateRealm(RealmConfiguration configuration, RealmMigration migration)
    static synchronized void migrateRealm(RealmConfiguration configuration)

    // New method for accessing schema
    RealmSchema getSchema();            

    // Old methods. Those with Class parameters have been converted to use String instead.
    void close();
    boolean isAutoRefresh();
    void setAutoRefresh(boolean autoRefresh);
    DynamicRealmObject createObject(String clazz);
    RealmQuery<DynamicRealmObject> where(String clazz);
    RealmResults<DynamicRealmObject> allObjects(String clazz);
    RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldName,
                                                             boolean sortAscending);
    RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldName1,
                                                             boolean sortAscending1, String fieldName2,
                                                             boolean sortAscending2);
    RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldName1,
                                                             boolean sortAscending1,
                                                             String fieldName2, boolean sortAscending2,
                                                             String fieldName3, boolean sortAscending3);
    RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldNames[],
                                                             boolean sortAscending[]);
    void addChangeListener(RealmChangeListener listener);
    void removeChangeListener(RealmChangeListener listener);
    void removeAllChangeListeners();
    void refresh();
    void beginTransaction();
    void commitTransaction();
    void cancelTransaction();
    void executeTransaction(DynamicRealm.Transaction transaction);
    void clear(String className);
    String getPath();
    RealmConfiguration getConfiguration();
}


*DynamicRealmObject*

public class DynamicRealmObject {

    // Generic get/set
    void set(String fieldName, Object value); // General set that automatically converts to proper type
    Object get(String fieldName); // General get

    // Typed get/set
    boolean getBoolean(String fieldName);
    int getInt(String fieldName);
    short getShort(String fieldName);
    long getLong(String fieldName);
    byte getByte(String fieldName);
    float getFloat(String fieldName);
    double getDouble(String fieldName);
    byte[] getBlob(String fieldName);
    String getString(String fieldName);
    Date getDate(String fieldName);
    DynamicRealmObject getObject(String fieldName);
    RealmList<DynamicRealmObjet> getList(String fieldName);
    void setBoolean(String fieldName, boolean value);
    void setShort(String fieldName, short value);
    void setInt(String fieldName, int value);
    void setLong(String fieldName, long value);
    void setByte(String fieldName, byte value);
    void setFloat(String fieldName, float value);
    void setDouble(String fieldName, double value);
    void setString(String fieldName, String value);
    void setBlob(String fieldName, byte[] value);
    void setDate(String fieldName, Date value);
    void setObject(String fieldName, DynamicRealmObject value);
    void setList(String fieldName, RealmList<DynamicRealmObjet> list);

    // Meta data information
    RealmFieldType getFieldType(String fieldName); // expose underlying Realm data type
    boolean isNull(String fieldName);
    boolean hasField(String fieldName);
    String[] getFieldNames();
    void removeFromRealm();
}

*RealmSchema*

public class RealmSchema {
    RealmObjectSchema getClass(String className);
    RealmObjectSchema addClass(String className);
    void removeClass(String className);
    RealmObjectSchema renameClass(String oldName, String newName);
    Set<RealmObjectSchema> getClasses();
}

*RealmObjectSchema*

public interface RealmObjectSchemaInterface {
    String getName();
    RealmObjectSchema addStringField(String fieldName);
    RealmObjectSchema addStringField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addShortField(String fieldName);
    RealmObjectSchema addShortField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addIntField(String fieldName);
    RealmObjectSchema addIntField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addLongField(String fieldName);
    RealmObjectSchema addLongField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addBooleanField(String fieldName);
    RealmObjectSchema addBooleanField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addByteArrayField(String fieldName);
    RealmObjectSchema addByteArrayField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addFloatField(String fieldName);
    RealmObjectSchema addFloatField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addDoubleField(String fieldName);
    RealmObjectSchema addDoubleField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addDateField(String fieldName);
    RealmObjectSchema addDateField(String fieldName, Set<RealmModifier> modifiers);
    RealmObjectSchema addObjectField(String fieldName, RealmObjectSchema objectSchema);
    RealmObjectSchema addListField(String fieldName, RealmObjectSchema objectSchema);
    RealmObjectSchema removeField(String fieldName);
    RealmObjectSchema renameField(String currentFieldName, String newFieldName);
    RealmObjectSchema addIndex(String fieldName);
    RealmObjectSchema removeIndex(String fieldName);
    boolean hasIndex(String fieldName);
    RealmObjectSchema addPrimaryKey(String fieldName);
    RealmObjectSchema removePrimaryKey();
    boolean hasPrimaryKey();
    RealmObjectSchema setNullable(String fieldName, boolean nullable);
    RealmFieldType getFieldType(String fieldName);
    String[] getFieldNames();
    RealmObjectSchema forEach(Iterator iterator);
}

