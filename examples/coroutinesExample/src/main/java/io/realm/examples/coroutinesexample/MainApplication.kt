package io.realm.examples.coroutinesexample

import androidx.multidex.MultiDexApplication
import io.realm.Realm

class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}
