package io.realm.examples.coroutinesexample.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.realm.Realm
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.model.Doggo

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
                .also { view ->
                    view.findViewById<Button>(R.id.buttonCount).setOnClickListener {
                        Realm.getDefaultInstance().use { realm ->
                            Log.e("--->", "---> ${Thread.currentThread().name} - count: ${realm.where(Doggo::class.java).count()}")
                        }
                    }
                    view.findViewById<Button>(R.id.buttonTransaction).setOnClickListener {
                        // Calling this multiple times before each transaction is done will not
                        // freeze the UI
                        viewModel.insertDogs()
                    }
                    view.findViewById<Button>(R.id.buttonDelete).setOnClickListener {
                        // Calling this while bulk-inserting will freeze the UI as executeTransactionAsync
                        // and deleteAll run on Realm's thread pool executor
                        viewModel.deleteAll()
                    }
                }
    }

    override fun onResume() {
        super.onResume()

        viewModel.getDogs().observe(viewLifecycleOwner, Observer { doggos ->
            doggos.forEach { doggo ->
                Log.e("--->", "---> ${Thread.currentThread().name} - $doggo")
            }
        })
    }
}
