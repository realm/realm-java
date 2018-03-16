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

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import io.realm.examples.arch.model.Person;
import io.realm.examples.arch.utils.ContextUtils;

public class PersonListFragment extends Fragment {
    public static PersonListFragment create() {
        return new PersonListFragment();
    }

    private RecyclerView recyclerView;
    private Adapter adapter;

    private PersonListViewModel personListViewModel;
    private List<Person> personList = Collections.emptyList();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragments should start listening in `onCreate()`
        // to ensure single observer instance, even if detached (for example in FragmentPagerAdapter).
        personListViewModel = ViewModelProviders.of(this).get(PersonListViewModel.class);
        personListViewModel.getPersons().observe(this, people -> {
            personList = people;
            if (adapter != null) {
                adapter.updateItems(people);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new Adapter(personList);
        recyclerView.setAdapter(adapter);
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private List<Person> persons;

        public Adapter(List<Person> persons) {
            this.persons = persons;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(persons.get(position));
        }

        @Override
        public int getItemCount() {
            return persons == null ? 0 : persons.size();
        }

        public void updateItems(List<Person> persons) {
            this.persons = persons;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView age;

            Person person;

            private final View.OnClickListener onClick = (view) -> {
                if (person == null) {
                    return;
                }
                AppCompatActivity activity = ContextUtils.findActivity(view.getContext());
                PersonFragment personFragment = PersonFragment.create(person.getName());
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.container, personFragment)
                        .addToBackStack(null)
                        .commit();
            };

            public ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.personName);
                age = itemView.findViewById(R.id.personAge);
                itemView.setOnClickListener(onClick);
            }

            public void bind(Person person) {
                this.person = person;
                name.setText(person.getName());
                age.setText(String.valueOf(person.getAge()));
            }
        }
    }
}
