package com.manak.eldercare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

//    override fun onCreate(savedInstanceState: Bundle?) {
//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task ->
//
//                if (task.isSuccessful) {
//                    android.util.Log.d("FCM_TOKEN", task.result)
//                }
//            }
//
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        bottomNav = findViewById(R.id.bottomNav)
//
//        if (savedInstanceState == null) {
//            loadFragment(HomeFragment())
//        }
//
//        bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home    -> { loadFragment(HomeFragment());    true }
//                R.id.nav_gps     -> { loadFragment(GpsFragment());     true }
//                R.id.nav_alerts  -> { loadFragment(AlertsFragment());  true }
//                R.id.nav_history -> { loadFragment(HistoryFragment()); true }
//                else -> false
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    android.util.Log.d("FCM_TOKEN", task.result)
                }
            }

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { loadFragment(HomeFragment()); true }
                R.id.nav_gps     -> { loadFragment(GpsFragment()); true }
                R.id.nav_alerts  -> { loadFragment(AlertsFragment()); true }
                R.id.nav_history -> { loadFragment(HistoryFragment()); true }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}