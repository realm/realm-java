package some.test;

import io.realm.RealmObject;

public class CustomAccessor extends RealmObject {

    public String getColumnString() {
        return "No associated field";
    }
}
