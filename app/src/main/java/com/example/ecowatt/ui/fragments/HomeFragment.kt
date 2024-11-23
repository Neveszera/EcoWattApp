package com.example.ecowatt.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.SensorResponse
import com.example.ecowatt.network.SensorResponsePage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var connectedSensorsText: TextView
    private lateinit var disconnectedSensorsText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gifImageView: ImageView = view.findViewById(R.id.gifImageView)
        connectedSensorsText = view.findViewById(R.id.connectedSensorsText)
        disconnectedSensorsText = view.findViewById(R.id.disconnectedSensorsText)

        val gifUrl = "https://i.ibb.co/ftW4r8w/gif-ecowatt.gif"

        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .into(gifImageView)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        val token = sharedPreferences.getString("auth_token", "")

        if (userId != -1 && !token.isNullOrEmpty()) {
            fetchSensorsFromApi(userId)
        } else {
            Toast.makeText(requireContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchSensorsFromApi(userId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitInstance.createAuthenticated(requireContext())
                val response = apiService.getSensorsByUserId(userId)

                if (response.isSuccessful && response.body() != null) {
                    val sensorsPage: SensorResponsePage = response.body()!!
                    val sensors = sensorsPage.content

                    // Filtrar os sensores conectados e desconectados
                    val connectedSensors = sensors.filter { it.status == "Conectado" }
                    val disconnectedSensors = sensors.filter { it.status == "Desconectado" }

                    withContext(Dispatchers.Main) {
                        // Atualizar os TextViews com a quantidade de sensores conectados e desconectados
                        connectedSensorsText.text = "${connectedSensors.size} / ${sensors.size}"
                        disconnectedSensorsText.text = "${disconnectedSensors.size} / ${sensors.size}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro ao buscar sensores: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
