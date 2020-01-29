package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;

// This class is only for creating the correct type hiearchy when testing Embedded Objects
// This class can work as a parent for all legal embedded object classes
public class EmbeddedClassParent extends RealmObject {
    public String name;
    public int age;

    // Valid children
    public EmbeddedClass child1;
    public EmbeddedClassRequiredParent child2;
    public EmbeddedClassOptionalParents child3;
    public EmbeddedClassOptionalParents child4;
}
