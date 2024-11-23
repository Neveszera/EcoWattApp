package com.example.ecowatt.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.UpdatePasswordRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NewPasswordFragment : Fragment(R.layout.fragment_new_password) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etCurrentPassword = view.findViewById<EditText>(R.id.et_current_password)
        val etNewPassword = view.findViewById<EditText>(R.id.et_new_password)
        val etConfirmNewPassword = view.findViewById<EditText>(R.id.et_confirm_new_password)
        val btnSubmit = view.findViewById<Button>(R.id.btn_submit_new_password)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        btnSubmit.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmNewPassword.text.toString().trim()

            // Validação da senha
            val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{7,}$".toRegex()
            if (!newPassword.matches(passwordPattern)) {
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

            // Verifica se a nova senha é igual à senha atual
            if (newPassword == currentPassword) {
                showAlertDialog("Erro", "A nova senha não pode ser igual à senha atual.")
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                showAlertDialog("Erro", "As senhas não coincidem.")
                return@setOnClickListener
            }

            updatePassword(userId, currentPassword, newPassword, confirmPassword)
        }

    }

    private fun updatePassword(userId: Int, currentPassword: String, newPassword: String, confirmPassword: String) {
        if (userId == -1) {
            showAlertDialog("Erro", "Usuário não autenticado. Por favor, faça login novamente.")
            return
        }

        lifecycleScope.launch {
            try {
                val request = UpdatePasswordRequest(currentPassword, newPassword, confirmPassword)
                val response = RetrofitInstance.createAuthenticated(requireContext()).updatePassword(userId, request)

                if (response.isSuccessful) {
                    showAlertDialog("Sucesso", "Senha atualizada com sucesso.") {
                        requireActivity().supportFragmentManager.popBackStack() // Volta para o fragmento anterior
                    }
                } else if (response.code() == 401) { // Código HTTP para "Não autorizado"
                    showAlertDialog("Erro", "Senha atual incorreta. Por favor, tente novamente.")
                } else {
                    Log.e("NewPasswordFragment", "Erro ao atualizar senha: ${response.code()} - ${response.message()}")
                    showAlertDialog("Erro", "Erro ao atualizar senha. Tente novamente mais tarde.")
                }
            } catch (e: HttpException) {
                Log.e("NewPasswordFragment", "Erro de HTTP: ${e.message()}", e)
                showAlertDialog("Erro", "Erro na comunicação com o servidor. Verifique sua conexão.")
            }
        }
    }

    // Função para exibir o AlertDialog
    private fun showAlertDialog(title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            onPositiveClick?.invoke()
        }
        builder.show()
    }
}
