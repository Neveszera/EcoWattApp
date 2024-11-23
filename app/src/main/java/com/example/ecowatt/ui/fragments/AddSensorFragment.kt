package com.example.ecowatt.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.SensorRequest
import kotlinx.coroutines.*

class AddSensorFragment : Fragment(R.layout.fragment_add_sensors) {

    private val apiService by lazy { RetrofitInstance.createAuthenticated(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSensorName = view.findViewById<EditText>(R.id.et_sensor_name)
        val spSensorType = view.findViewById<Spinner>(R.id.et_sensor_type)
        val etSensorLocation = view.findViewById<EditText>(R.id.et_sensor_location)
        val etSensorProduct = view.findViewById<EditText>(R.id.et_sensor_product)
        val etSensorDescription = view.findViewById<EditText>(R.id.et_sensor_description)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtem o estado do sistema
        val isSystemConnected = !sharedPreferences.getBoolean("system_off", false)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nextSensorName = getNextSensorName(userId)
                withContext(Dispatchers.Main) {
                    etSensorName.setText(nextSensorName)
                    etSensorName.isEnabled = false
                }
            } catch (e: Exception) {
                Log.e("AddSensorFragment", "Erro ao obter próximo nome do sensor: ${e.message}")
            }
        }

        // Configuração do spinner
        val sensorTypes = resources.getStringArray(R.array.sensor_types)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sensorTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSensorType.adapter = adapter
        spSensorType.setSelection(0)

        btnSave.setOnClickListener {
            val sensorName = etSensorName.text.toString()
            val sensorType = spSensorType.selectedItem.toString()
            val sensorLocation = etSensorLocation.text.toString()
            val sensorProduct = etSensorProduct.text.toString()
            val sensorDescription = etSensorDescription.text.toString()

            if (sensorType == "Escolha um item da lista" || sensorProduct.isBlank()) {
                Toast.makeText(requireContext(), "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Define o status do sensor
            val sensorStatus = if (isSystemConnected) "Conectado" else "Desconectado"
            CoroutineScope(Dispatchers.IO).launch {
                val sensorRequest = SensorRequest(
                    tipoSensor = sensorType,
                    nomeSensor = sensorName,
                    status = sensorStatus,
                    produtoConectado = sensorProduct,
                    descricao = if (sensorDescription.isBlank()) "-" else sensorDescription,
                    localizacao = if (sensorLocation.isBlank()) "-" else sensorLocation,
                    usuarioId = userId
                )

                try {
                    val response = apiService.registerSensor(sensorRequest)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            navigateToResultFragment(true)
                        } else {
                            Log.e("AddSensorFragment", "Erro ao cadastrar sensor: ${response.errorBody()?.string()}")
                            navigateToResultFragment(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AddSensorFragment", "Erro ao tentar cadastrar o sensor: ${e.message}")
                    withContext(Dispatchers.Main) {
                        navigateToResultFragment(false)
                    }
                }
            }
        }
    }

    private fun navigateToResultFragment(isSuccess: Boolean) {
        val fragment = if (isSuccess) AddSuccessFragment() else AddFailedFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SensorsFragment())
                .commit()
        }
    }

    private suspend fun getNextSensorName(userId: Int): String {
        return try {
            val response = apiService.getSensorsByUserId(userId)
            if (response.isSuccessful && response.body() != null) {
                val totalSensors = response.body()!!.totalElements
                "ESP32 - ${"%03d".format(totalSensors + 1)}"
            } else {
                "ESP32 - 001"
            }
        } catch (e: Exception) {
            "ESP32 - 001"
        }
    }
}
