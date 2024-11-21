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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecowatt.R
import com.example.ecowatt.adapter.SensorAdapter
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.SensorResponse
import com.example.ecowatt.network.SensorResponsePage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SensorsFragment : Fragment(R.layout.fragment_sensors) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var btnAdd: ImageView
    private lateinit var btnAddBottom: ImageView
    private lateinit var adapter: SensorAdapter
    private var userId: Int = -1
    private var token: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        emptyText = view.findViewById(R.id.empty_text)
        btnAdd = view.findViewById(R.id.btn_add)
        btnAddBottom = view.findViewById(R.id.btn_add_bottom)

        adapter = SensorAdapter(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)
        token = sharedPreferences.getString("auth_token", "")

        if (userId != -1 && !token.isNullOrEmpty()) {
            fetchSensorsFromApi(userId)
        } else {
            Toast.makeText(requireContext(), "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show()
        }

        val addSensorFragment = AddSensorFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        btnAdd.setOnClickListener {
            transaction.replace(R.id.fragment_container, addSensorFragment)
                .addToBackStack(null)
                .commit()
        }

        btnAddBottom.setOnClickListener {
            transaction.replace(R.id.fragment_container, addSensorFragment)
                .addToBackStack(null)
                .commit()
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

                    withContext(Dispatchers.Main) {
                        if (sensors.isNotEmpty()) {
                            emptyText.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.updateSensors(sensors)
                            btnAdd.visibility = View.GONE
                            btnAddBottom.visibility = View.VISIBLE
                        } else {
                            emptyText.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            btnAdd.visibility = View.VISIBLE
                            btnAddBottom.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro ao buscar sensores: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onSensorUpdated(updatedSensor: SensorResponse) {
        adapter.updateSensor(updatedSensor)
    }
}

