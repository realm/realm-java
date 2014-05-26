package io.realm.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import io.realm.example.entities.RoyalPerson;
import io.realm.typed.Realm;

public class RoyalExample extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tView = new TextView(this);
        setContentView(tView);

        // Initialize the Realm
        Realm realm = new Realm(this);

        try {

            realm.beginWrite();

            realm.clear();

            realm.add(new RoyalPerson("King", 47, true));
            realm.add(new RoyalPerson("Queen", 44, true));
            realm.add(new RoyalPerson("Prince", 27, true));
            realm.add(new RoyalPerson("Princess", 22, false));
            realm.add(new RoyalPerson("QueenMother", 85, true));

            realm.commit();

        } catch(Throwable t) {
            t.printStackTrace();
            realm.rollback();
        }

        double averageAgeOfPeopleWithHorses = realm.where(RoyalPerson.class).equalTo("hashorse", true).averageInt("age");

        tView.append(averageAgeOfPeopleWithHorses + "\n");

        try {
            realm.beginWrite();

            RoyalPerson princess = realm.where(RoyalPerson.class).equalTo("title", "Princess").findFirst();
            princess.setHasHorse(true);

            realm.commit();
        } catch(Throwable t) {
            t.printStackTrace();
            realm.rollback();
        }


        averageAgeOfPeopleWithHorses = realm.where(RoyalPerson.class).equalTo("hashorse", true).averageInt("age");

        tView.append(averageAgeOfPeopleWithHorses + "\n");

    }

}
