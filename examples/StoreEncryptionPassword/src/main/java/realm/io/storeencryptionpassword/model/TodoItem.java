package realm.io.storeencryptionpassword.model;
import io.realm.RealmObject;
/**
 * Created by Nabil on 12/04/2016.
 */
public class TodoItem extends RealmObject {
    private String name;
    private boolean isDone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }
}
