package com.example.ecowatt.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.SensorRequest
import kotlinx.coroutines.*

class SystemOffFragment : Fragment(R.layout.fragment_system_off) {

    private val apiService by lazy { RetrofitInstance.createAuthenticated(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOff = view.findViewById<Button>(R.id.btn_off)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        btnOff.setOnClickListener {
            // Atualizar status dos sensores para "Desconectado"
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.getSensorsByUserId(userId)
                    if (response.isSuccessful && response.body() != null) {
                        val sensors = response.body()!!.content

                        // Atualizar cada sensor para "Desconectado"
                        sensors.forEach { sensor ->
                            val updatedSensor = SensorRequest(
                                tipoSensor = sensor.tipoSensor,
                                status = "Desconectado",
                                nomeSensor = sensor.nomeSensor,
                                produtoConectado = sensor.produtoConectado,
                                descricao = sensor.descricao,
                                localizacao = sensor.localizacao,
                                usuarioId = userId
                            )
                            apiService.updateSensor(sensor.id, updatedSensor)
                        }

                        // Navegar para tela de sucesso
                        withContext(Dispatchers.Main) {
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, SystemOffSuccessFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Erro ao obter sensores", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack() // Voltar ao fragmento anterior
        }
    }
}
