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

These objects should be instantiated from the RealmCollection using the method create()

## Instatiating a Realm

To get started with realm we create a new instance of a Realm

    Realm realm = new Realm(context);

RealmList works as a traditional List, which is just automatically persisted in Realm.
The Context, is used to get access to a writeable directory on the device, and the Class is used to generate the underlying datamodel.

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


## Querying

A benefit to using Realm is that you get access to a fast query interface

To find all users named "John" you would write

    RealmList<User> result = realm.where(User.class).equalTo("name", "John").findAll();

This gives you a new list, of users with the name John

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


