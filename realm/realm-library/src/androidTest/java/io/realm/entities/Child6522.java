package io.realm.entities;

import java.util.Objects;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by Nickolay Semendyaev on 09.07.2017.
 */
public class Child6522 extends RealmObject {
    @Index
    private Integer id;
    @Index
    private int category;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Child6522 that = (Child6522) o;
        return category == that.category &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category);
    }
}
