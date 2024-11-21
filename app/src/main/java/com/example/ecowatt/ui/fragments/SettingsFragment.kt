package com.example.ecowatt.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.UpdatePasswordRequest
import com.example.ecowatt.ui.auth.LoginActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOff = view.findViewById<Button>(R.id.btn_off)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        val btnDeleteAccount = view.findViewById<Button>(R.id.btn_delete_account)
        val btnUpdatePassword = view.findViewById<Button>(R.id.btn_update_password)
        val etCurrentPassword = view.findViewById<EditText>(R.id.et_current_password)
        val etNewPassword = view.findViewById<EditText>(R.id.et_new_password)
        val etConfirmNewPassword = view.findViewById<EditText>(R.id.et_confirm_new_password)

        btnOff.setOnClickListener {
            navigateToSystemOffFragment()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        btnUpdatePassword.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmNewPassword.text.toString()

            if (newPassword == confirmPassword) {
                updatePassword(currentPassword, newPassword, confirmPassword)
            } else {
                Toast.makeText(requireContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToSystemOffFragment() {
        val systemOffFragment = SystemOffFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, systemOffFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Logout")
            .setMessage("Você tem certeza que deseja deslogar?")
            .setPositiveButton("Sim") { _, _ -> logoutUser() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun logoutUser() {
        // Remover o token de autenticação e limpar as preferências do usuário
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("auth_token")
        editor.remove("user_id")
        editor.apply()

        // Redireciona para a tela de login
        Toast.makeText(requireContext(), "Deslogado com sucesso", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Finaliza a atividade atual
    }

    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Exclusão de Conta")
            .setMessage("Você tem certeza que deseja excluir sua conta? Esta ação é irreversível.")
            .setPositiveButton("Sim") { _, _ -> deleteUserAccount() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deleteUserAccount() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.createAuthenticated(requireContext()).deleteUser(userId)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Conta deletada com sucesso", Toast.LENGTH_SHORT).show()
                        logoutUser() // Deslogar após excluir a conta
                    } else {
                        Toast.makeText(requireContext(), "Erro ao deletar a conta", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: HttpException) {
                    Toast.makeText(requireContext(), "Erro na comunicação com o servidor", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Usuário não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        // Regex para validação de senha
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{7,250}$".toRegex()

        // Verifica se a nova senha e a confirmação são iguais
        if (newPassword != confirmPassword) {
            Toast.makeText(requireContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            return
        }

        // Valida a nova senha com o regex
        if (!newPassword.matches(passwordPattern)) {
            Toast.makeText(
                requireContext(),
                "A nova senha deve ter pelo menos uma letra maiúscula, uma letra minúscula, um número, um caractere especial e no mínimo 7 caracteres.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Recupera o ID do usuário
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        // Se o ID do usuário for válido, continua a atualização da senha
        if (userId != -1) {
            val request = UpdatePasswordRequest(currentPassword, newPassword, confirmPassword)

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.createAuthenticated(requireContext()).updatePassword(userId, request)

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show()
                    } else {
                        // Log adicional para verificar a resposta do servidor
                        Log.e("UpdatePassword", "Erro ao atualizar a senha: ${response.code()} - ${response.message()}")
                        Toast.makeText(requireContext(), "Erro ao atualizar a senha", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: HttpException) {
                    // Log do erro para mais detalhes
                    Log.e("UpdatePassword", "Erro de HTTP: ${e.message()}", e)
                    Toast.makeText(requireContext(), "Erro na comunicação com o servidor", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // Log do erro geral (para outros tipos de exceções)
                    Log.e("UpdatePassword", "Erro inesperado: ${e.message}", e)
                    Toast.makeText(requireContext(), "Erro inesperado ao atualizar a senha", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Usuário não encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
