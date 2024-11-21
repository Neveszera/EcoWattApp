package com.example.ecowatt.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecowatt.MainActivity
import com.example.ecowatt.R
import com.example.ecowatt.network.RetrofitInstance
import com.example.ecowatt.network.SensorResponse
import com.example.ecowatt.ui.fragments.EditSensorFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SensorAdapter(
    private val context: Context,
) : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    private var sensorList: MutableList<SensorResponse> = mutableListOf()

    // Mapa de ícones associados ao tipo de sensor
    private val sensorIconMap = mapOf(
        "Geração de energia" to R.drawable.ic_energy_generation,
        "Fonte de uso" to R.drawable.ic_usage_source,
        "Comunicação" to R.drawable.ic_communication,
        "Linha de distribuição" to R.drawable.ic_distribution_line,
        "Fonte de energia (tomada ou lâmpada)" to R.drawable.ic_power_source
    )

    // Método para atualizar a lista de sensores
    fun updateSensors(sensors: List<SensorResponse>) {
        sensors.forEach { newSensor ->
            if (sensorList.none { it.id == newSensor.id }) {
                sensorList.add(newSensor)
            }
        }
        notifyDataSetChanged()
    }

    fun updateSensor(updatedSensor: SensorResponse) {
        val index = sensorList.indexOfFirst { it.id == updatedSensor.id }
        if (index != -1) {
            sensorList[index] = updatedSensor
            notifyItemChanged(index)
        } else {
            sensorList.add(updatedSensor)
            notifyItemInserted(sensorList.size - 1)
        }
    }

    inner class SensorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val simpleLayout: View = view.findViewById(R.id.simple_layout)
        val expandedLayout: View = view.findViewById(R.id.expanded_layout)
        val sensorName: TextView = view.findViewById(R.id.sensor_name)
        val signal: TextView = view.findViewById(R.id.signal)
        val connectedProduct: TextView = view.findViewById(R.id.connected_product)
        val status: TextView = view.findViewById(R.id.status)
        val consumption: TextView = view.findViewById(R.id.consumption)
        val arrowIcon: ImageView = view.findViewById(R.id.arrow_icon)
        val editIcon: ImageView = view.findViewById(R.id.edit_icon)
        val deleteIcon: ImageView = view.findViewById(R.id.delete_icon)
        val iconSensor: ImageView = view.findViewById(R.id.icon_sensor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.sensor_item, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = sensorList[position]

        // Atualiza as informações do sensor
        holder.sensorName.text = sensor.nomeSensor
        holder.connectedProduct.text = "Produto conectado: ${sensor.produtoConectado}"
        holder.status.text = "Status: ${sensor.status}"

        // Atribui valores aleatórios para sinal e consumo
        val randomSignal = Random.nextInt(0, 101)
        val randomConsumption = Random.nextInt(2, 16)
        holder.signal.text = "Sinal: $randomSignal%"
        holder.consumption.text = "Consumo: $randomConsumption Watts / hora"

        // Define o ícone com base no tipo do sensor
        val iconResId = sensorIconMap[sensor.tipoSensor]
        if (iconResId != null) {
            holder.iconSensor.setImageResource(iconResId)
        } else {
            holder.iconSensor.setImageResource(R.drawable.ic_energy_generation) // Ícone padrão
        }

        // Alterna entre layouts simples e expandidos
        var isExpanded = false
        holder.simpleLayout.setOnClickListener {
            isExpanded = !isExpanded
            holder.expandedLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            holder.arrowIcon.setImageResource(
                if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            )
            holder.simpleLayout.setBackgroundResource(
                if (isExpanded) R.drawable.background_item_green else R.drawable.background_item
            )
            holder.sensorName.setTextColor(
                if (isExpanded) context.getColor(android.R.color.white) else context.getColor(android.R.color.black)
            )
        }

        // Edita o sensor
        holder.editIcon.setOnClickListener {
            val fragment = EditSensorFragment().apply {
                arguments = Bundle().apply {
                    putInt("sensorId", sensor.id)
                    putString("nomeSensor", sensor.nomeSensor)
                    putString("tipoSensor", sensor.tipoSensor)
                    putString("status", sensor.status)
                    putString("produtoConectado", sensor.produtoConectado)
                    putString("localizacao", sensor.localizacao)
                    putString("descricao", sensor.descricao)
                }
            }
            val activity = context as? MainActivity
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        // Deleta o sensor
        holder.deleteIcon.setOnClickListener {
            val sensorId = sensorList[position].id
            val apiService = RetrofitInstance.createAuthenticated(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.deleteSensor(sensorId)
                    if (response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            sensorList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = sensorList.size
}
