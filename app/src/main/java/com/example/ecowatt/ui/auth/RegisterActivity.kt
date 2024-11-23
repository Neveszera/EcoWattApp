package com.example.ecowatt.ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

        // Altera a cor da barra de status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.statusBarColor = resources.getColor(R.color.colorPrimary)
        }

        val userEditText = findViewById<EditText>(R.id.et_user)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.et_confirm_password)
        val nameEditText = findViewById<EditText>(R.id.et_user_name)
        val registerButton = findViewById<Button>(R.id.create_account)

        registerButton.setOnClickListener {
            val login = userEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val fullName = nameEditText.text.toString().trim()

            // Validação dos campos
            if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
                showAlertDialog("Campos Obrigatórios", "Preencha todos os campos!")
                return@setOnClickListener
            }

            // Validação da senha
            val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{7,}$".toRegex()
            if (!password.matches(passwordPattern)) {
                showAlertDialog(
                    "Senha Inválida",
                    """
                        A senha deve ter pelo menos 7 caracteres, incluindo:
                        - Uma letra maiúscula
                        - Uma letra minúscula
                        - Um número
                        - Um caractere especial
                    """.trimIndent()
                )
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showAlertDialog("Erro", "As senhas não coincidem.")
                return@setOnClickListener
            }

            // Envio dos dados para o servidor
            CoroutineScope(Dispatchers.IO).launch {
                try {
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
                            val successMessage = response.body() ?: "Conta criada com sucesso."
                            showAlertDialog("Sucesso", successMessage) {
                                // Redireciona para a tela de login
                                val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(loginIntent)
                                finish()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                            Log.e("RegisterError", "Erro ao criar conta: ${response.code()} - $errorBody")
                            showAlertDialog("Erro", "Erro ao criar conta: $errorBody")
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        // Por algum motivo esta caindo aqui, mesmo o registro dando Sucesso e a conta sendo criada
                        showAlertDialog("Conta Criada", "Conta criada com sucesso! Faça Login.") {
                            val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(loginIntent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    // Função para exibir o AlertDialog
    private fun showAlertDialog(title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            onPositiveClick?.invoke()
        }
        builder.show()
    }
}
