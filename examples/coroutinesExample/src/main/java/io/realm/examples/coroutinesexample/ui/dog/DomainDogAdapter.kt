package io.realm.examples.coroutinesexample.ui.dog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.domain.model.DomainDog

class DomainDogAdapter : ListAdapter<DomainDog, DomainDogAdapter.DogViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder =
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dog, parent, false)
                    .let { view -> DogViewHolder(view) }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = getItem(position)
        holder.dogName.text = dog.name
        holder.dogAge.text = dog.age.toString()
    }

    inner class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dogName: TextView = view.findViewById(R.id.dogName)
        val dogAge: TextView = view.findViewById(R.id.dogAge)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DomainDog>() {
            override fun areItemsTheSame(oldItem: DomainDog, newItem: DomainDog): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: DomainDog, newItem: DomainDog): Boolean =
                    oldItem.name == newItem.name && oldItem.age == newItem.age
        }
    }
}
