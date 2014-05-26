package io.realm.typed.entities;

        import io.realm.typed.RealmObject;

public class Dog extends RealmObject {

    private String name;
    private int age;
    private Coordinates coords;

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

    public Coordinates getCoords() {
        return coords;
    }

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }
}
