package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.movicard.databinding.ActivityLoginBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.network.RetrofitInstanceAPI
import kotlinx.coroutines.launch
import java.security.MessageDigest

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
                var psswdHashed = hasheaPassword(password)
                verificarUsuario(email, psswdHashed)
            }
        }

        var isPasswordVisible = false

        binding.eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Mostrar contraseña
                binding.password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.eyeIcon.setImageResource(R.drawable.eye_open) // Cambia a ícono de ojo abierto
            } else {
                // Ocultar contraseña
                binding.password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyeIcon.setImageResource(R.drawable.eye_closed) // Cambia a ícono de ojo cerrado
            }
            // Mueve el cursor al final
            binding.password.setSelection(binding.password.text.length)
        }
    }

    fun hasheaPassword(password: String): String {
        val salt = "M0v¡ç@rD!6%"
        val message = salt + password
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(message.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun verificarUsuario(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // obtengo todos los clientes
                val clientes = RetrofitInstanceAPI.api.getAllClientes()

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