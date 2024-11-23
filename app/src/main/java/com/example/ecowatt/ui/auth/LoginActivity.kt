package com.example.ecowatt.ui.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecowatt.MainActivity
import com.example.ecowatt.R
import com.example.ecowatt.network.LoginRequest
import com.example.ecowatt.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Altera a cor da barra de status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.statusBarColor = resources.getColor(R.color.colorPrimary)
        }

        val loginButton = findViewById<Button>(R.id.login_button)
        val etUser = findViewById<EditText>(R.id.et_user)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val createAccountText = findViewById<TextView>(R.id.create_account)

        loginButton.setOnClickListener {
            val login = etUser.text.toString().trim()
            val senha = etPassword.text.toString().trim()

            if (login.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Faz a requisição de login
                    val response = RetrofitInstance.create().loginUser(LoginRequest(login, senha))
                    runOnUiThread {
                        if (response.isSuccessful) {
                            val token = response.body()?.token
                            if (!token.isNullOrEmpty()) {
                                val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                sharedPreferences.edit().apply {
                                    putBoolean("is_authenticated", true)
                                    putString("auth_token", token)
                                    putInt("user_id", response.body()?.idUsuario ?: -1)
                                    apply()
                                }

                                Toast.makeText(this@LoginActivity, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                                val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(mainIntent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Token não encontrado.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Tratamento de erros com base no código HTTP
                            when (response.code()) {
                                400 -> {
                                    val errorBody = response.errorBody()?.string() ?: ""
                                    if (errorBody.contains("usuário não encontrado", ignoreCase = true)) {
                                        Toast.makeText(this@LoginActivity, "Usuário não encontrado.", Toast.LENGTH_SHORT).show()
                                    } else if (errorBody.contains("senha incorreta", ignoreCase = true)) {
                                        Toast.makeText(this@LoginActivity, "Senha incorreta.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@LoginActivity, "Erro de validação: $errorBody", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else -> {
                                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                                    Toast.makeText(this@LoginActivity, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        createAccountText.setOnClickListener {
            val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(registerIntent)
        }
    }
}
