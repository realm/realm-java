package io.realm.examples.compatibility

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_activty)
    }
}
