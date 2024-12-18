package com.muhammedaliderindag.sarisayfalar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.muhammedaliderindag.sarisayfalar.databinding.ActivityGirisBinding
import com.muhammedaliderindag.sarisayfalar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var navHostFragment: NavHostFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        if (navHostFragment != null) {
            val navController = navHostFragment!!.navController
            NavigationUI.setupWithNavController(binding.bottomNav, navController)

        }

    }
}