package com.example.ecowatt.ui.auth

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecowatt.R
import com.example.ecowatt.network.RegisterRequest
import com.example.ecowatt.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val userEditText = findViewById<EditText>(R.id.et_user)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.et_confirm_password)
        val nameEditText = findViewById<EditText>(R.id.et_user_name)
        val registerButton = findViewById<Button>(R.id.create_account)

        registerButton.setOnClickListener {
            val login = userEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val fullName = nameEditText.text.toString()

            if (password == confirmPassword) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Cria a instância do Retrofit sem autenticação
                        val apiService = RetrofitInstance.create()

                        val response = apiService.registerUser(
                            RegisterRequest(
                                login = login,
                                senha = password,
                                nomeCompleto = fullName,
                                senhaConfirmacao = confirmPassword
                            )
                        )

                        runOnUiThread {
                            if (response.isSuccessful) {
                                Toast.makeText(this@RegisterActivity, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                finish() // Fecha a tela após o registro
                            } else {
                                val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                                Toast.makeText(this@RegisterActivity, "Erro ao criar conta: ${response.message()}", Toast.LENGTH_SHORT).show()
                                Log.d("RegisterError", "Erro ao criar conta: $errorBody")
                                Log.d("RegisterError", "Erro ao criar conta: ${response.errorBody()?.string()}")
                                Log.d("RegisterRequest", "Login: $login, Password: $password, Full Name: $fullName")
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
