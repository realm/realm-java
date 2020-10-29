package io.realm.examples.coroutinesexample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.examples.coroutinesexample.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private val dogAdapter = DomainDogAdapter()

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = FragmentMainBinding.inflate(inflater, container, false)
            .also { binding ->
                this.binding = binding
                setupRecyclerView()
                setupLiveData()
            }.root

    private fun setupRecyclerView() {
        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = dogAdapter
        }

        with(binding.refresh) {
            setOnRefreshListener {
                viewModel.refreshDogs()
            }
        }
    }

    private fun setupLiveData() {
        viewModel.dogs.observe(viewLifecycleOwner, Observer { dogs ->
            if (binding.refresh.isRefreshing) {
                binding.refresh.setRefreshing(false)
            }
            dogAdapter.submitList(dogs)
        })
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
