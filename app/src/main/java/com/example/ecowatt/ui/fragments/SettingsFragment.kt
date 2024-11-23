package com.example.ecowatt.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.SensorRequest
import com.example.ecowatt.ui.auth.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOff = view.findViewById<Button>(R.id.btn_off)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        val btnDeleteAccount = view.findViewById<Button>(R.id.btn_delete_account)
        val btnUpdatePassword = view.findViewById<Button>(R.id.btn_update_password)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val apiService = RetrofitInstance.createAuthenticated(requireContext())
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica o estado do sistema
        var isSystemOff = sharedPreferences.getBoolean("system_off", false)
        updateButtonState(btnOff, isSystemOff)

        btnOff.setOnClickListener {
            if (isSystemOff) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val response = apiService.getSensorsByUserId(userId)
                        if (response.isSuccessful && response.body() != null) {
                            val sensors = response.body()!!.content
                            sensors.forEach { sensor ->
                                val updatedSensor = SensorRequest(
                                    tipoSensor = sensor.tipoSensor,
                                    status = "Conectado",
                                    nomeSensor = sensor.nomeSensor,
                                    produtoConectado = sensor.produtoConectado,
                                    descricao = sensor.descricao,
                                    localizacao = sensor.localizacao,
                                    usuarioId = userId
                                )
                                apiService.updateSensor(sensor.id, updatedSensor)
                            }
                            withContext(Dispatchers.Main) {
                                isSystemOff = false
                                sharedPreferences.edit().putBoolean("system_off", isSystemOff).apply()
                                updateButtonState(btnOff, isSystemOff)
                                Toast.makeText(requireContext(), "Sistema ligado com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e("SettingsFragment", "Erro ao ligar o sistema: ${e.message}", e)
                            Toast.makeText(requireContext(), "Erro ao ligar o sistema", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                navigateToSystemOffFragment()
            }
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        btnUpdatePassword.setOnClickListener {
            navigateToNewPasswordFragment() // Redireciona para o fragmento de nova senha
        }
    }

    private fun updateButtonState(button: Button, isSystemOff: Boolean) {
        if (isSystemOff) {
            button.text = "Ligar sistema"
            button.setBackgroundColor(resources.getColor(R.color.color_on))
        } else {
            button.text = "Desligar sistema"
            button.setBackgroundColor(resources.getColor(R.color.color_off))
        }
    }

    private fun navigateToSystemOffFragment() {
        val systemOffFragment = SystemOffFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, systemOffFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToNewPasswordFragment() {
        val newPasswordFragment = NewPasswordFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newPasswordFragment)
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
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("auth_token")
        editor.remove("user_id")
        editor.apply()

        Toast.makeText(requireContext(), "Deslogado com sucesso", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
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
                        logoutUser()
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
}
