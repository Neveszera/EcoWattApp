package com.example.ecowatt.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ecowatt.R

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botão para desligar o sistema
        val btnOff = view.findViewById<Button>(R.id.btn_off)

        btnOff.setOnClickListener {
            // Redireciona para o fragmento de confirmação
            navigateToSystemOffFragment()
        }
    }

    private fun navigateToSystemOffFragment() {
        val systemOffFragment = SystemOffFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, systemOffFragment)
            .addToBackStack(null) // Para permitir voltar ao SettingsFragment
            .commit()
    }
}
