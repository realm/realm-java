/*
 * Copyright 2018 Realm Inc.
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
package io.realm.examples.arch;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PersonFragment extends Fragment {
    private static final String ARG_PERSON_NAME = "personName";

    public static PersonFragment create(String personName) {
        PersonFragment personFragment = new PersonFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PERSON_NAME, personName);
        personFragment.setArguments(bundle);
        return personFragment;
    }

    private PersonViewModel personViewModel;

    private TextView name;
    private TextView age;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @SuppressWarnings("ConstantConditions") final String personName = getArguments().getString(ARG_PERSON_NAME);
        personViewModel = ViewModelProviders.of(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass == PersonViewModel.class) {
                    PersonViewModel personViewModel = new PersonViewModel();
                    personViewModel.setup(personName); // we use a Factory to ensure `setup` is called before use.
                    //noinspection unchecked
                    return (T) personViewModel;
                }
                //noinspection ConstantConditions
                return null;
            }
        }).get(PersonViewModel.class);

        personViewModel.getPerson().observe(this, person -> {
            if (person != null) { // null would mean the object was deleted.
                name.setText(person.getName());
                age.setText(String.valueOf(person.getAge()));
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name = view.findViewById(R.id.personName);
        age = view.findViewById(R.id.personAge);
    }
}
