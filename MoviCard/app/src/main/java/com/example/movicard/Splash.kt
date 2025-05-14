package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Splash : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo_splash)

        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)
        logo.startAnimation(animation)

        // Ejecutar el código con un retraso en el hilo principal sin bloquear la interfaz.
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(
                this@Splash,
                Login::class.java
            )
            startActivity(intent)
            finish()
        }, 2000) // 2000 ms = 2 segundos
    }
}