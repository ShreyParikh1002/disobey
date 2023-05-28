package com.example.disobey

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class Dashboard : AppCompatActivity() {
    lateinit var chipNavigationBar :ChipNavigationBar;
//    lateinit var chipNavigationBarMain :ChipNavigationBar;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        chipNavigationBar = findViewById(R.id.nav)
        chipNavigationBar.setItemSelected(
            R.id.bottom_nav_stats,
            true
        )
//        chipNavigationBarMain = findViewById(R.id.main_nav)
//        chipNavigationBarMain.setItemSelected(
//            R.id.bottom_nav_dashboard,
//            true
//        )
//        chipNavigationBarMain.setOnItemSelectedListener { id ->
//            when (id) {
//                R.id.bottom_nav_leaderboards -> {
//                    val intent = Intent(this@Dashboard, Leaderboard::class.java)
//                    startActivity(intent)
//                    true
//                }
//                R.id.bottom_nav_map -> {
//                    val intent = Intent(this@Dashboard, MainActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
//
//                // Add more cases for other navigation items if needed
//                else -> false
//            }
//
//        }

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                Stats()
            ).commit()
        bottomMenu()
    }
    private fun bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener { id ->
            val fragment: Fragment = when (id) {
                R.id.bottom_nav_stats -> Stats()
                R.id.bottom_nav_backpack -> Backpack()
                else -> throw IllegalArgumentException("Invalid menu item ID")
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}