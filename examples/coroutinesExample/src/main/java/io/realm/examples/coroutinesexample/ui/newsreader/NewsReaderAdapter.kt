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

package io.realm.examples.coroutinesexample.ui.newsreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.realm.examples.coroutinesexample.R
import io.realm.examples.coroutinesexample.data.newsreader.local.room.RoomNYTimesArticle

class NewsReaderAdapter : ListAdapter<RoomNYTimesArticle, NewsReaderAdapter.ArticleViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder =
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_article, parent, false)
                    .let { view -> ArticleViewHolder(view) }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)
        holder.title.text = article.title
    }

    inner class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RoomNYTimesArticle>() {
            override fun areItemsTheSame(oldItem: RoomNYTimesArticle, newItem: RoomNYTimesArticle): Boolean =
                    oldItem == newItem

            override fun areContentsTheSame(oldItem: RoomNYTimesArticle, newItem: RoomNYTimesArticle): Boolean =
                    oldItem.title == newItem.title && oldItem.abstract == newItem.abstract
        }
    }
}


///*
// * Copyright 2020 Realm Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.realm.examples.coroutinesexample.ui.newsreader
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import io.realm.examples.coroutinesexample.R
//import io.realm.examples.coroutinesexample.data.newsreader.local.realm.RealmNYTimesArticle
//
//class NewsReaderAdapter : ListAdapter<RealmNYTimesArticle, NewsReaderAdapter.ArticleViewHolder>(DIFF_CALLBACK) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder =
//            LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_article, parent, false)
//                    .let { view -> ArticleViewHolder(view) }
//
//    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
//        val article = getItem(position)
//        holder.title.text = article.title
//    }
//
//    inner class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val title: TextView = view.findViewById(R.id.title)
//    }
//
//    companion object {
//        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RealmNYTimesArticle>() {
//            override fun areItemsTheSame(oldItem: RealmNYTimesArticle, newItem: RealmNYTimesArticle): Boolean =
//                    oldItem == newItem
//
//            override fun areContentsTheSame(oldItem: RealmNYTimesArticle, newItem: RealmNYTimesArticle): Boolean =
//                    oldItem.title == newItem.title && oldItem.abstract == newItem.abstract
//        }
//    }
//}
