/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.examples.threads;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.UUID;

import io.realm.Realm;
import io.realm.examples.threads.model.Person;

public class PassingObjectsFragment extends Fragment {

    private TextView textContent;

    private Realm realm;
    private Person person;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passing_objects, container, false);
        textContent = (TextView) view.findViewById(R.id.text_content);

        setListeners(view);
        return view;
    }

    /**
     * Sets the click listeners on the three buttons.
     *
     * RealmObjects are thread confined, therefore they cannot be passed through an intent. We
     * recommend using an object identifier and passing that value through the intent. The
     * receiving Android component will then obtain that value, open the Realm and query for
     * that object.
     *
     * Below, each {@link android.view.View.OnClickListener} builds an {@link Intent} that
     * accepts the {@link Person#getId()} value as a string extra. This is the value that will
     * be retrieved in the receiving Android components ({@link ReceivingActivity},
     * {@link ReceivingService} and {@link WakefulReceivingBroadcastReceiver}.
     *
     * @param view The view where the buttons are located.
     */
    private void setListeners(View view) {
        view.findViewById(R.id.start_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReceivingActivity.class);
                intent.putExtra("person_id", person.getId());
                startActivity(intent);
            }
        });

        view.findViewById(R.id.start_intent_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReceivingService.class);
                intent.putExtra("person_id", person.getId());
                getActivity().startService(intent);
            }
        });

        view.findViewById(R.id.start_receiver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WakefulReceivingBroadcastReceiver.class);
                intent.putExtra("person_id", person.getId());
                getActivity().sendBroadcast(intent);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                person = realm.createObject(Person.class, UUID.randomUUID().toString());
                person.setName("Jane");
                person.setAge(42);
            }
        });
        textContent.setText(person.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(Person.class);
            }
        });
        // Clear out all Person instances.
        realm.close();
    }
}

