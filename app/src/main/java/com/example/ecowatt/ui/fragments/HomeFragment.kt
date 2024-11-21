package com.example.ecowatt.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ecowatt.R
import android.widget.ImageView

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gifImageView: ImageView = view.findViewById(R.id.gifImageView)

        val gifUrl = "https://i.ibb.co/ftW4r8w/gif-ecowatt.gif"

        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .into(gifImageView)
    }
}
