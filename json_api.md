## API Methods


### Android

    Realm.createObjectFromJson(Class realmObject, JSONObject json)
    Realm.createAllFromJson(Class realmObject, JSONArray json)

    Realm.createObjectFromJson(Class realmObject, InputStream json);
    Realm.createAllFromJson(Class realmObject, InputStream json);


### Java

*Not implemented yet*


## Notes about the JSON API


- Import using InputStream only works for API 11+. Methods are annotated as such.

- JSON property names need to match java variabel names.

- Import API does not work in standard Java. JSON is only supported from Java 7, and with a
  different API.

- If import fails, everything up to that point is put in the database (if Realm.commitTransaction()
  is called). We need a Realm.cancelWriteTransaction() to change this behavior.

- createObject() returns the created Object, createAll() doesn't. Returning it matches the current
  API, but not being able to for Arrays is annoying. Does it make sense to return created object
  anyway? As it is already filled.

- Currently two methods are added to RealmObject: populateUsingJsonObject() and
  populateUsingJsonStream(). They are overridden in the proxy objects with the proper
  implementations. It would perhaps be better to move these to a separate class, but then it makes
  it harder to hook it into the Realm object.


### Enhancements

- adding Realm.rollback() or Realm.cancelWriteTransaction() will make it possible to abort a faulty
  import.

- Java 6 does not have Json parsing capabilities. We need to add them ourselves. Perhaps using GSON.

- Realm.createOrUpdateFromJson() would be a natural extension but requires primary key support.

- Consider adding mapping annotations between Json and Java, but is this a Realm responsibility?

