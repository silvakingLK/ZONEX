package com.example.zonex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val statusBarSpacer = view.findViewById<View>(R.id.statusBarSpacer)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val spacerParams = statusBarSpacer.layoutParams
            spacerParams.height = systemBars.top
            statusBarSpacer.layoutParams = spacerParams
            insets
        }

        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val ivAccessory = view.findViewById<android.widget.ImageView>(R.id.ivAccessory)

        fun updateAccessory() {
            val accessory = prefs.getString("accessory", "none")
            when(accessory) {
                "pumpkin" -> {
                    ivAccessory.visibility = View.VISIBLE
                    ivAccessory.setImageResource(R.drawable.ic_pumpkin)
                }
                "visor" -> {
                    ivAccessory.visibility = View.VISIBLE
                    ivAccessory.setImageResource(R.drawable.ic_visor)
                }
                else -> ivAccessory.visibility = View.GONE
            }
        }

        // Carrega o cosmético inicial salvo
        updateAccessory()

        view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEquipPumpkin).setOnClickListener {
            val current = prefs.getString("accessory", "none")
            if (current == "pumpkin") {
                prefs.edit().putString("accessory", "none").apply() // Desequipa
            } else {
                prefs.edit().putString("accessory", "pumpkin").apply() // Equipa
            }
            updateAccessory()
        }

        view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEquipVisor).setOnClickListener {
            val current = prefs.getString("accessory", "none")
            if (current == "visor") {
                prefs.edit().putString("accessory", "none").apply() // Desequipa
            } else {
                prefs.edit().putString("accessory", "visor").apply() // Equipa
            }
            updateAccessory()
        }
    }
}