package com.example.ecowatt.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.SensorRequest
import com.example.ecowatt.network.SensorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.SharedPreferences
import android.util.Log
import com.example.ecowatt.adapter.SensorAdapter

class EditSensorFragment : Fragment(R.layout.fragment_edit_sensors) {

    private val apiService by lazy { RetrofitInstance.createAuthenticated(requireContext()) }
    private var sensorId: Int = -1  // ID do sensor a ser editado
    private var sensorAdapter: SensorAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSensorName = view.findViewById<EditText>(R.id.et_sensor_name)
        val spSensorType = view.findViewById<Spinner>(R.id.et_sensor_type)
        val etSensorLocation = view.findViewById<EditText>(R.id.et_sensor_location)
        val etSensorProduct = view.findViewById<EditText>(R.id.et_sensor_product)
        val etSensorDescription = view.findViewById<EditText>(R.id.et_sensor_description)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        // Verificando se o fragmento está anexado antes de acessar o contexto
        if (!isAdded || context == null) {
            Toast.makeText(requireContext(), "Erro: fragmento não anexado", Toast.LENGTH_SHORT).show()
            Log.e("EditSensorFragment", "Fragmento não anexado ao contexto ou contexto nulo")
            return
        }

        // Usando 'activity?.let { ... }' para garantir que a atividade não seja nula
        activity?.let { context ->
            // Obtendo o ID do usuário das preferências compartilhadas
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("user_id", -1)

            if (userId == -1) {
                Toast.makeText(context, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show()
                Log.e("EditSensorFragment", "Usuário não autenticado. ID: $userId")
                return
            }

            // Carregar os dados do sensor a ser editado
            sensorId = arguments?.getInt("sensorId", -1) ?: -1
            if (sensorId == -1) {
                Toast.makeText(requireContext(), "Erro: ID de sensor inválido", Toast.LENGTH_SHORT).show()
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sensorResponse = getSensorById(sensorId) // Obtendo os dados do sensor
                    withContext(Dispatchers.Main) {
                        etSensorName.setText(sensorResponse.nomeSensor)
                        spSensorType.setSelection(getSensorTypePosition(sensorResponse.tipoSensor))
                        etSensorLocation.setText(sensorResponse.localizacao)
                        etSensorProduct.setText(sensorResponse.produtoConectado)
                        etSensorDescription.setText(sensorResponse.descricao)
                    }
                } catch (e: Exception) {
                    Log.e("EditSensorFragment", "Erro ao obter os dados do sensor: ${e.message}")
                }
            }

            // Configurando o adaptador para o Spinner
            val sensorTypes = resources.getStringArray(R.array.sensor_types)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sensorTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spSensorType.adapter = adapter

            btnSave.setOnClickListener {
                val sensorName = etSensorName.text.toString()
                val sensorType = spSensorType.selectedItem.toString()
                val sensorLocation = etSensorLocation.text.toString()
                val sensorProduct = etSensorProduct.text.toString()
                val sensorDescription = etSensorDescription.text.toString()

                if (sensorType == "Escolha um item da lista" || sensorProduct.isBlank()) {
                    Toast.makeText(requireContext(), "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                    Log.w("EditSensorFragment", "Campos obrigatórios não preenchidos: Tipo do sensor ou produto")
                    return@setOnClickListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val sensorRequest = SensorRequest(
                        tipoSensor = sensorType,
                        nomeSensor = sensorName,
                        status = "Conectado",  // Supondo que o status não mude
                        produtoConectado = sensorProduct,
                        descricao = if (sensorDescription.isBlank()) "-" else sensorDescription,
                        localizacao = if (sensorLocation.isBlank()) "-" else sensorLocation,
                        usuarioId = userId
                    )

                    try {
                        Log.d("EditSensorFragment", "Enviando requisição para atualizar o sensor: $sensorRequest")
                        val response = apiService.updateSensor(sensorId, sensorRequest)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Sensor atualizado com sucesso!", Toast.LENGTH_SHORT).show()

                                // Atualizar a lista de sensores no Adapter
                                sensorAdapter?.updateSensor(response.body() ?: return@withContext)
                                requireActivity().supportFragmentManager.popBackStack()
                            } else {
                                val statusCode = response.code()
                                val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                                Log.e("EditSensorFragment", "Erro ao atualizar o sensor. Status Code: $statusCode, Mensagem: $errorMessage")
                                Toast.makeText(requireContext(), "Erro ao atualizar o sensor: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("EditSensorFragment", "Erro ao tentar atualizar o sensor: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // Função para obter o sensor pelo ID
    private suspend fun getSensorById(sensorId: Int): SensorResponse {
        return try {
            val response = apiService.getSensorById(sensorId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Erro ao obter o sensor")
            }
        } catch (e: Exception) {
            Log.e("EditSensorFragment", "Erro ao obter o sensor: ${e.message}")
            throw e
        }
    }

    // Função para mapear o tipo do sensor para a posição do Spinner
    private fun getSensorTypePosition(sensorType: String): Int {
        val sensorTypes = resources.getStringArray(R.array.sensor_types)
        return sensorTypes.indexOf(sensorType).takeIf { it >= 0 } ?: 0
    }
}
