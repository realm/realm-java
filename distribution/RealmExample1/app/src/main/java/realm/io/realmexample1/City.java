package realm.io.realmexample1;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass
public class City extends RealmObject {

    private String name;
    private long   votes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }
}
