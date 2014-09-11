package realm.io.realmexample1;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass
public class City extends RealmObject {

    private String name;
    private Long votes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVotes() {
        return votes;
    }

    public void setVotes(Long votes) {
        this.votes = votes;
    }
}
