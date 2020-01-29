package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass(embedded = true)
public class EmbeddedClassMissingFinalOnLinkingObjects extends RealmObject {
    public String name;
    public int age;

    @LinkingObjects("child5")
    public EmbeddedClassParent parent;
}
