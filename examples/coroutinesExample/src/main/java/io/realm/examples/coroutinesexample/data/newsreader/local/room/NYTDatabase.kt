package io.realm.examples.coroutinesexample.data.newsreader.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
        entities = [RoomNYTimesArticle::class],
        version = 1
)
abstract class NYTDatabase: RoomDatabase() {
    abstract fun nytDao(): RoomNYTDao
}
