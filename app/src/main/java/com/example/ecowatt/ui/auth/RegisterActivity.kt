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
                                // Exibe a mensagem de sucesso
                                Toast.makeText(this@RegisterActivity, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                finish() // Fecha a tela após o registro
                            } else {
                                // Exibe o erro se a resposta não for bem-sucedida
                                val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                                Log.e("RegisterError", "Erro ao criar conta: ${response.code()} - $errorBody")
                                Toast.makeText(this@RegisterActivity, "Erro ao criar conta: $errorBody", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        // Captura de exceções
                        runOnUiThread {
                            Log.e("RegisterError", "Erro de conexão: ${e.printStackTrace()}")
                            Toast.makeText(this@RegisterActivity, "Erro de conexão, tente novamente.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
