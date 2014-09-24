package io.realm.examples.performance.ormlite;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Employee")
public class ORMLiteEmployee {

    @DatabaseField(generatedId = true) int id;
    @DatabaseField String name;
    @DatabaseField int age;
    @DatabaseField boolean hired;

    public ORMLiteEmployee() {
        // Required by OrmLite
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isHired() {
        return hired;
    }

    public void setHired(boolean hired) {
        this.hired = hired;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
