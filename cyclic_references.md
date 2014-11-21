# Discussion about cyclic references in RealmObjects

Cyclic references are a problem as they very easily can cause StackOverflowErrors in code.
Problematic methods are toString(), hashCode() and equals()

Example code:

    public class CyclicType extends RealmObject {

        private String name;
        private CyclicType object;
        private RealmList<CyclicType> objects;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public CyclicType getObject() {
            return object;
        }

        public void setObject(CyclicType object) {
            this.object = object;
        }

        public RealmList<CyclicType> getObjects() {
            return objects;
        }

        public void setObjects(RealmList<CyclicType> objects) {
            this.objects = objects;
        }
    }

    Realm realm = Realm.getInstance(getContext());
    realm.beginTransaction();
    realm.clear(CyclicType.class);
    CyclicType foo = realm.createObject(CyclicType.class);
    foo.setName("Foo");
    CyclicType bar = realm.createObject(CyclicType.class);
    bar.setName("Bar");

    // Setup cycle on normal object references
    foo.setObject(bar);
    bar.setObject(foo);

    realm.commitTransaction();

    foo.hashCode() // Boom!
    foo.equals(bar) // Boom!
    foo.toString() // Boom!


Solutions for the 3 different methods are discussed below

##toString()

- Don't follow objects, just show their RowIndex
- Don't follow RealmLists, just show their Ids

Output from foo.toString():

    [name=Foo,object=1,objects=[]]


##hashCode()

A hashcode has the following rules:

- foo.equals(bar) -> foo.hashCode() == bar.hashCode()
- foo.hashCode() == bar.hashCode() -/> foo.equals(bar)
- hashcode is the same across threads.
- hashCode does not have to be consistent between application restarts.
- HashCode does not have to be equal between processes.

This means we can use same semantic as for equals:

    HashCode = 17 + realm.getPath().hashCode()*31 + table.getName().hashCode()*31 + rowIndex*31

or something to that effect.

##equals()

Objects in Realm are considered equal if they point to the same data in the database, otherwise not.
This have some implications:

- Test if realm/table/rowIndex is equal => Equal objects
- Once we implement Primary keys: Two are objects are equal if they have same primary key.
- RealmObject on two different threads are always equal, but it is impossible to compare them.
- Same object in two Realms are never equal.
- Standalone object and object in Realm are never equal.


## Discussion points

- Standalone objects. If they are introduced you cannot compare them to objects already in Realm. Is that fair?
- People cannot override equals() for RealmObjects. Problematic?


## References

Some additional reading with discussion/solutions on the topic.

http://www.avaje.org/equals.html
http://www.onjava.com/pub/a/onjava/2006/09/13/dont-let-hibernate-steal-your-identity.html?page=1
http://docs.jboss.org/hibernate/core/4.0/manual/en-US/html/persistent-classes.html#persistent-classes-equalshashcode
http://stackoverflow.com/questions/8863308/implementing-equals-and-hashcode-for-objects-with-circular-references-in-java
