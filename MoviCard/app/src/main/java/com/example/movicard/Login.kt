package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.movicard.databinding.ActivityLoginBinding
import com.example.movicard.databinding.ActivityPrincipalBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.network.RetrofitInstance
import kotlinx.coroutines.launch

class Login : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.email.error = "Campo requerido"
                binding.password.error = "Campo requerido"
            } else {
                verificarUsuario(email, password)
            }
        }
    }

    private fun verificarUsuario(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // obtengo todos los clientes
                val clientes = RetrofitInstance.api.getAllClientes()

                // verifico si el correo que introduje coincide con algún correo de un cliente
                val cliente = clientes.find { it.correo == email }

                // mensaje de rror si el correo o la contraseña no coinciden
                if (cliente == null) {
                    binding.email.error = "Correo no encontrado"
                } else if (cliente.password != password) {
                    binding.password.error = "Contraseña incorrecta"
                } else {

                    // USUARIO CORRECTO

                    // tengo el permiso de entrar a la app
                    val intent = Intent(this@Login, Principal::class.java)

                    // Guardo la sesión del cliente correcto para así llenar la app con sus datos
                    val sessionManager = SessionManager(this@Login)
                    sessionManager.saveCliente(cliente)

                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@Login, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }
}