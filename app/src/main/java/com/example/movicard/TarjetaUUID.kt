package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.movicard.databinding.ActivityTarjetaUuidBinding

class TarjetaUUID : AppCompatActivity() {
    private lateinit var binding: ActivityTarjetaUuidBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.tarjeta
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)
    }

    private val bottomNavListener = fun(item: MenuItem): Boolean{
        when (item.itemId) {
            R.id.home -> {
                // Cambia a Home
                startActivity(Intent(this, Principal::class.java))
                return true
            }
        }
        return false
    }
}