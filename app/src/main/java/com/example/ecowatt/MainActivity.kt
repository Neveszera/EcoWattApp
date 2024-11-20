package com.example.ecowatt

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.SharedPreferences
import com.example.ecowatt.ui.auth.LoginActivity
import com.example.ecowatt.ui.fragments.AnalysesFragment
import com.example.ecowatt.ui.fragments.EnergyFragment
import com.example.ecowatt.ui.fragments.HomeFragment
import com.example.ecowatt.ui.fragments.SensorsFragment
import com.example.ecowatt.ui.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isAuthenticated = sharedPreferences.getBoolean("is_authenticated", false)

        if (isAuthenticated) {
            setupBottomNavigation(savedInstanceState)
        } else {
            navigateToLogin()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBottomNavigation(savedInstanceState: Bundle?) {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_analyses -> {
                    loadFragment(AnalysesFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                R.id.nav_middle -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_sensors -> {
                    loadFragment(SensorsFragment())
                    true
                }
                R.id.nav_energy -> {
                    loadFragment(EnergyFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }
}
