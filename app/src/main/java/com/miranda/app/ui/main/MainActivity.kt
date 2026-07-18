package com.miranda.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.miranda.app.R
import com.miranda.app.ui.dashboard.DashboardFragment
import com.miranda.app.ui.tariffs.TariffsFragment
import com.miranda.app.ui.services.ServicesFragment
import com.miranda.app.ui.payments.PaymentsFragment
import com.miranda.app.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Устанавливаем первый фрагмент при запуске
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        // Обработчик переключения вкладок
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.navigation_tariffs -> {
                    loadFragment(TariffsFragment())
                    true
                }
                R.id.navigation_services -> {
                    loadFragment(ServicesFragment())
                    true
                }
                R.id.navigation_payments -> {
                    loadFragment(PaymentsFragment())
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}