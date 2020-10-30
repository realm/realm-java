package io.realm.examples.coroutinesexample.ui.dog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dropbox.android.external.store4.StoreResponse
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.databinding.FragmentMainBinding
import io.realm.examples.coroutinesexample.domain.model.DomainDog

class DogFragment : Fragment() {

    private val viewModel: DogViewModel by viewModels()
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
        viewModel.storeResponse.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is StoreResponse.Loading -> StateHelper.loading(binding)
                is StoreResponse.Data -> StateHelper.data(binding, response, dogAdapter)
                is StoreResponse.NoNewData -> StateHelper.noNewData(binding)
                is StoreResponse.Error.Exception -> StateHelper.errorException(binding, response.error)
                is StoreResponse.Error.Message -> StateHelper.errorMessage(binding, response)
            }
        })
    }

    private object StateHelper {
        fun loading(binding: FragmentMainBinding) {
            if (!binding.refresh.isRefreshing) {
                binding.refresh.setRefreshing(true)
            }
        }

        fun data(binding: FragmentMainBinding, response: StoreResponse.Data<List<DomainDog>>, dogAdapter: DomainDogAdapter) {
            if (binding.refresh.isRefreshing) {
                binding.refresh.setRefreshing(false)
            }
            dogAdapter.submitList(response.value)
        }

        fun noNewData(binding: FragmentMainBinding) {
            // do nothing...?
        }

        fun errorException(binding: FragmentMainBinding, throwable: Throwable) {
            val stacktrace = throwable.cause?.stackTrace?.joinToString { "$it\n" }
            Log.e(TAG, "--- error (exception): ${throwable.message} - ${throwable.cause?.message}: $stacktrace")
            Toast.makeText(binding.root.context, R.string.error_generic, Toast.LENGTH_SHORT).show()
        }

        fun errorMessage(binding: FragmentMainBinding, response: StoreResponse.Error.Message) {
            Log.e(TAG, "--- error (message): ${response.message}")
            Toast.makeText(binding.root.context, R.string.error_generic, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance() = DogFragment()
    }
}
