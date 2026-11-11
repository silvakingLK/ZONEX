package com.example.zonex

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment

class VictoryDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_DISTANCE = "distance"
        private const val ARG_TIME = "time"
        private const val ARG_PACE = "pace"
        private const val ARG_XP = "xp"
        private const val ARG_AREA = "area"
        private const val ARG_ZONES = "zones"

        fun newInstance(
            distance: String,
            time: String,
            pace: String,
            xp: String,
            area: String,
            zones: String
        ): VictoryDialogFragment {
            val fragment = VictoryDialogFragment()
            val args = Bundle()
            args.putString(ARG_DISTANCE, distance)
            args.putString(ARG_TIME, time)
            args.putString(ARG_PACE, pace)
            args.putString(ARG_XP, xp)
            args.putString(ARG_AREA, area)
            args.putString(ARG_ZONES, zones)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_victory, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            // Fundo transparente para a moldura customizada da imagem e 90% de largura
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val distance = arguments?.getString(ARG_DISTANCE) ?: "0.00 KM"
        val time = arguments?.getString(ARG_TIME) ?: "00:00:00"
        val pace = arguments?.getString(ARG_PACE) ?: "0'00\" /KM"
        val xp = arguments?.getString(ARG_XP) ?: "+0 XP"
        val area = arguments?.getString(ARG_AREA) ?: "0 m²"
        val zones = arguments?.getString(ARG_ZONES) ?: "0 ZONAS"

        view.findViewById<TextView>(R.id.tvSummaryDistance).text = "DISTÂNCIA: $distance"
        view.findViewById<TextView>(R.id.tvSummaryTime).text = "TEMPO: $time"
        view.findViewById<TextView>(R.id.tvSummaryPace).text = "RITMO: $pace"
        view.findViewById<TextView>(R.id.tvSummaryArea).text = "ÁREA DOMINADA: $area"
        view.findViewById<TextView>(R.id.tvSummaryZones).text = "CONQUISTAS: $zones"
        view.findViewById<TextView>(R.id.tvSummaryXp).text = "XP GANHO: $xp"

        view.findViewById<AppCompatButton>(R.id.btnShare).setOnClickListener {
            shareVictory(distance, time, pace, area, zones)
        }

        view.findViewById<AppCompatButton>(R.id.btnNext).setOnClickListener {
            dismiss()
        }
    }

    private fun shareVictory(distance: String, time: String, pace: String, area: String, zones: String) {
        val shareText = """
            🏃‍♂️ Acabei de completar uma missão no ZoneX!
            
            📍 Distância: $distance
            ⏱️ Tempo: $time
            ⚡ Ritmo: $pace
            🗺️ Zonas Dominadas: $zones
            🏙️ Área Conquistada: $area
            
            Junte-se à resistência e domine seu território! 🟩⬛
        """.trimIndent()

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        
        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Vitória")
        startActivity(shareIntent)
    }
}