package com.example.ecowatt.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import com.example.ecowatt.R

class AddFailedFragment : Fragment(R.layout.fragment_add_failed) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aguarde 3 segundos antes de redirecionar para a lista de sensores
        Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SensorsFragment())
                .commit()
        }, 3000) // 3 segundos
    }
}
