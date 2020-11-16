/*
 * Copyright 2020 Realm Inc.
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

package io.realm.examples.coroutinesexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import io.realm.examples.coroutinesexample.ui.details.DetailsFragment
import io.realm.examples.coroutinesexample.ui.main.MainFragment
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MainActivity : AppCompatActivity(), MainFragment.OnItemClicked {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            showMainFragment()
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is MainFragment -> fragment.onItemclickedCallback = this
        }
    }

    override fun onBackPressed() {
        val detailsFragment = supportFragmentManager.findFragmentByTag(DetailsFragment::class.java.simpleName)
        if (detailsFragment != null) {
            showMainFragment()
        } else {
            super.onBackPressed()
        }
    }

    override fun onItemClicked(id: String) {
        val fragment = DetailsFragment.instantiate(DetailsFragment.ArgsBundle(id))
        supportFragmentManager.commit {
            setCustomAnimations(
                    R.anim.fragment_open_enter,
                    R.anim.fragment_open_exit
            )
            replace(R.id.container, fragment, DetailsFragment.TAG)
            addToBackStack(null)
        }
    }

    private fun showMainFragment() {
        supportFragmentManager.commit {
            replace(R.id.container, MainFragment.newInstance(), DetailsFragment.TAG)
        }
    }
}
