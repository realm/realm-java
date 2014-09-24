package io.realm.examples.performance.sugar_orm;

import com.orm.SugarRecord;

public class SugarEmployee extends SugarRecord<SugarEmployee> {

    private String name;
    private int age;
    private int hired;

    public SugarEmployee() {

    }

    public SugarEmployee(String name, int age, int hired) {
        this.name  = name;
        this.age   = age;
        this.hired = hired;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getHired() {
        return hired;
    }

    public void setHired(int hired) {
        this.hired = hired;
    }
}
