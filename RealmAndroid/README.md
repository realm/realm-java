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

## Instatiating a Realm Collection

To get started with realm we create a new instance of a RealmList, this can be done through a static factory

    RealmList<User> users = Realms.list(context, User.class);

RealmList works as a traditional List, which is just automatically persisted in Realm.
The Context, is used to get access to a writeable directory on the device, and the Class is used to generate the underlying datamodel.

## Adding Objects

To add a new object to the RealmList, you add it like you normally do, the only new thing is the way the object is instantiated

	User user = users.create();

This gives us a new User instance, which we can then populate with data, before adding it to the list.

    user.setId(0);
    user.setName("John");
    user.setEmail("john@corporation.com");

    users.add(user);


## Getting Objects

To retrieve an object from the list

    Users user = users.get(0);

This is just like we are used to from a List, the difference here is that the User object is backed by Realm, which means that when you change its values it is automatically persisted.

## Iterating through Objects

To make a run through all objects in a RealmList you can use a traditional for loop

    for(int i = 0; i < users.size(); i++) {
        User u = users.get(i);
        ... do something with the object ...
    }

Or you can take advantage of Iterable

    for(User u : users) {
        ... do something with the object ...
    }

## Querying

A benefit to using Realm Collections is that you get access to a fast query interface, which enables you to filter the collection

To find all users named "John" you would write

    RealmList<User> result = users.where().equalTo("name", "John");

This gives you a new list, of users with the name John
