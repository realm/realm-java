package io.realm.examples.realmintroexample.model;

import java.lang.String;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Ignore;

@RealmClass
public class Person extends RealmObject {

    private String name;
    private int age;

    private Dog dog;

    @Ignore private String codeWord;

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

    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    public String getCodeWord() {
        return codeWord;
    }

    public void setCodeWord(String codeWord) {
        this.codeWord = codeWord;
    }

}