package com.scurab.android.spotifyrc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        supportFragmentManager
            .takeIf { it.findFragmentById(R.id.fragment_container) == null }
            ?.let {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container, HomeFragment())
                }
            }
    }
}