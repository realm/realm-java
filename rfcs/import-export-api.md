# Import / Export API

This document describe a possible new Import/Export API that is more generalized
than our current JSON support and is intended to replace it.

## Motivation

Currently Realm Java exposes a number of JSON methods (12) for importing JSON
data into Realm. These works fine, but are limited in functionality plus they
bloat our public API quite considerably as well as adding a fair amount of code
to our proxy classes.

There is general agreement internally that Realm Java should not be a full JSON
parser. Other JSON libraries are much more focused, better tested and with more
features. The tipping point was https://github.com/realm/realm-java/issues/1470,
which we all agree is a very useful feature, but also a slippery slope for
turning into a full-fledged JSON library.

So instead of just keep piling features onto our JSON support we would much
rather look at this from a broader perspective of getting data in and out of
Realm through a more generalized Import/Export API. Such a API should be flexible
enough to support most common data formats.


## Use cases - High level

a) Import JSON from common sources: String, JSONObject/JSONArray, JsonReader.
    1) Users of our current JSON API should be able to migrate without too much
       trouble.
	2) Support things like `@SerializedName`
	3) Support automatically setting up references, e.g by just having the
	   primary key value in JSON as a reference.

b) Be compatible with common JSON libraries: GSON, Jackson, Moshi.
	1) It should be possible to auto-generate TypeAdapters.

c) Import data from other formats like CSV, XML and SQLite databases.

e) Export data to common formats: CSV, XML, JSON


## Use cases - Already supported

Realm already have `copyToRealm()/copyFromRealm()` methods that uses in-memory
Java objects as an intermediate representation.

This means that any library that either output or accept in-memory object is
supported by Realm Java today. An incomplete list is:

- Retrofit
- GSON
- Jackson
- Moshi
- Other annotation processors: Realm does not consume annotations enabling other
  tools to also generate code, e.g.: https://github.com/cmelchior/realmfieldnameshelper

This means that any support we offer is mostly to be able to provide a more
efficient/lightweight solution + support for features not offered by other
libraries.

## Use cases - Not currently supported

- Export directly to JSON without needing a 3rd party library
- Export to CSV and XML
- Custom support for `@SerializableName`
- Support for automatically find references from ID's.

## Implementation

### Current API's

3 variants of the below 4 methods. JsonReader variant only supported on API 11+.

```
realm.createObjectFromJson(Class, <String/JSONObject/JsonReader>)
realm.createOrUpdateObjectFromJson(Class, <String/JSONObject/JsonReader>)
realm.createAllFromJson(Class, <String/JSONArray/JsonReader>)
realm.createOrUpdateAllFromJson(Class, <String/JSONArray/JsonReader>)
```

All these methods require that field names match 1:1. 


### Proposal A

* All Import/Export methods are done using a specialized class or classes.
* This class is fully decoupled from Realm and is purely an addon.

**New Classes**
* Add new `RealmJson` class. Will behave similar to the `Gson` class
  (https://google.github.io/gson/apidocs/com/google/gson/Gson.html).
* Add new `ObjectWriter` interface for outputting objects to streams/files.
* Add new `@SerializedName` annotation for mapping between names.


```java
// build.gradle
dependencies {
	compile 'io.realm:android-json-support:1.0.0' // Made up name
} 

// Example
// New class that defines both input/output
// Up for debate if this class is static or instantiated.
// This example requires a normal instance (same as GSON) to enable multiple
// different setups.

public class MyApplication extends Application {

 	public static RealmJson realmJson;

	public void onCreate() {
		super.onCreate();
		Realm.init();
		realmJson = new RealmJson(); // Could also be a builder for extra options
	}

}

// Usage
Realm realm = Realm.getDefaultInstance();

// Import
// Naming not easy to get right: `import/importAndUpdate, create/createOrUpdate,
// insert/insertOrUpdate`, ...
realm.beginTransaction();
realmJson.insert(realm, Person.class, getJsonStream());
realmJson.insertOrUpdate(realm, Person.class, getJsonString());
realm.commitTransaction();

// Export
RealmResults<Person> results = realm.where(Person.class).findAll();
String json = realmJson.export(results); // Convert to String
realmJson.exportToFile(results, new File("path/to/file"))
realmJson.export(results, new CustomObjectWriter("path/to/file"));
```

**Advantages**
- Complete decoupling from Realm
- New formats supported easily by creating `RealmXml`, `RealmCsv`, ...
- Big reduction in Realm Proxy class size and complexity
- Big reduction in API surface for the Realm class
- Move towards a opt-in plugin-type architecture.

**Disadvantages**
- Harder to discover functionality. Must visit website to find it.
- Harder to manage versions, e.g. which version of `RealmJson` is compatible
  with Realm Java?
- Confusing API that you need to use `Realm` to create the transaction, but
  `realmJson` to insert data.


### Proposal B

* Generalize the current `insert/insertOrUpdate` API but allow you to register
  "type-adapters" for different formats.
* Add new `export` API.
* Possible to use realm config block to configure plugins.

**New Classes**
* New `ObjectReader` interface that all importers must implemenent
* Add new `ObjectWriter` interface for outputting objects to streams/files.
* New API methods
    
    ```
    // Will automatically try to detect importer based on type of object
    realm.insert(Class, Object); 
    realm.insertOrUpdate(Class, Object)
    // Disambiguate if needed, e.g String could both be JSON or XML
    realm.insert(Class, Object, String)
    realm.insertOrUpdate(Class, Object, String)
	```

```java
// build.gradle
realm {
	jsonSupport = true
	xmlSupport = true
	csvSupport = true
	....
}

// Example
public class MyApplication extends Application {

 	public static RealmJson realmJson;

	public void onCreate() {
		super.onCreate();
		// Will automatically detect any of the officially supported extension
		Realm.init();
	}
}

// Usage
Realm realm = Realm.getDefaultInstance();

// Import
realm.beginTransaction();
realm.insert(Person.class, getXMLString(), "xml");
realm.insert(Person.class, getJSONString(), "json");
// Automatically detect which importer to use
realm.insertOrUpdate(Person.class, getJsonStream());
realm.commitTransaction();

// Export
RealmResults<Person> results = realm.where(Person.class).findAll();
String data = Realm.export(results, "xml"); // Convert to String
// Interface for serializing to whatever
realmJson.export(results, new JsonObjectWriter("path/to/file"));
```

**Advantages**
- API visible in main Realm API.
- Actual implementation still fully opt-in.
- New formats supported easily by creating `RealmXml`, `RealmCsv`, ...
- Big reduction in Realm Proxy class size and complexity
- Big reduction in API surface for the Realm class
- Move towards a opt-in plugin-type architecture.

**Disadvantages**
- Methods will crash if no plugin is installed. 
- Must visit website to find versions if not using realm config block
- Specifying "type" as a String makes it hard to discover legal values.
- Less flexible than a seperate class.


## Open questions:

* Can we enumerate possible input formats (XML, JSON, CSV), or does it need to
  be flexible?

* A more radical solution could also be to just remove our JSON support
  completely and completely delegate it to 3rd party libraries?









