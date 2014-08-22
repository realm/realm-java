## Building

### CORE

    cd tightdb
    git checkout master
    sh build.sh build
    sh build.sh build-android
    sudo sh build.sh install

### Object store

    cd ../tightdb_java
    git checkout object-store-master
    TIGHTDB_JAVA_VERSION=1.7 sh build.sh config
    sh build.sh build
    sh build.sh build-android
    sh build.sh android-package
    mkdir -p RealmAndroid/lib/libs
    cp lib/realm-android-*.jar RealmAndroid/lib/libs

Edit (or create) the `RealmAndroid/local.properties` file with contents:

    sdk.dir=<path to your sdk>

And now run

    cd RealmAndroid
    gradle build

To run on a connected device:

    gradle connectedAndroidTest

and use `adb logcat` to see output.



## Defining a Data Model

Realm data models are defined by implementing a traditional Java Bean (http://en.wikipedia.org/wiki/Java_Bean)

	public class User {

        private int id;
        private String name;
        private String email;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

    }

These objects should be instantiated from the Realm using the create() method

## Instatiating a Realm

To get started with realm we create a new instance of a Realm

    Realm realm = new Realm(context);


## Adding Objects

To add a new object to the Realm, we instantiate it through the Realm

	User user = realm.create(User.class);

This gives us a new User instance, which we can populate with data

    user.setId(0);
    user.setName("John");
    user.setEmail("john@corporation.com");

### Alternative way of adding objects

An alternative to this would be to add an already existing object to the Realm

    User user = new User();
    user.setId(0);
    user.setName("John");
    user.setEmail("john@corporation.com");

    user = realm.add(user);

## Transactions
All write operations must be wrapped in write transactions to work

    try {
        realm.beginWrite();

        ... do writes here ...

        realm.commit();
    } catch(Throwable t) {
        realm.rollback();
    }

## Querying

A benefit to using Realm is that you get access to a fast query interface

To find all users named "John" you would write

    RealmList<User> result = realm.where(User.class).equalTo("name", "John").findAll();

This gives you a new RealmList, of users with the name John

## Iterating through Objects

To make a run through all objects in a RealmList you can use a traditional for loop

    for(int i = 0; i < result.size(); i++) {
        User u = result.get(i);
        ... do something with the object ...
    }

Or you can take advantage of Iterable

    for(User u : result) {
        ... do something with the object ...
    }


