package com.mongodb.realm.example;

import io.realm.mongodb.User;

import static com.mongodb.realm.example.MyApplicationKt.APP;

public class Test {

    public void test() {
        User user = APP.getEmailPasswordAuth().registerUser(username, password);

    }
}
