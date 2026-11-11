package com.example.zonex

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TerritoryDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_ZONE_NAME = "zone_name"
        private const val ARG_OWNER_NAME = "owner_name"
        private const val ARG_AREA = "area"
        private const val ARG_LEVEL = "level"

        fun newInstance(zoneName: String, ownerName: String, area: String, level: String): TerritoryDetailBottomSheet {
            val fragment = TerritoryDetailBottomSheet()
            val args = Bundle()
            args.putString(ARG_ZONE_NAME, zoneName)
            args.putString(ARG_OWNER_NAME, ownerName)
            args.putString(ARG_AREA, area)
            args.putString(ARG_LEVEL, level)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_territory_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Torna o fundo padrão do BottomSheet transparente
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)

        val zoneName = arguments?.getString(ARG_ZONE_NAME) ?: "Território Desconhecido"
        val ownerName = arguments?.getString(ARG_OWNER_NAME) ?: "Anon"
        val areaText = arguments?.getString(ARG_AREA) ?: "Área: -- m²"
        val levelText = arguments?.getString(ARG_LEVEL) ?: "Nível 1"

        view.findViewById<TextView>(R.id.tvZoneName).text = zoneName
        view.findViewById<TextView>(R.id.tvOwnerName).text = ownerName
        view.findViewById<TextView>(R.id.tvArea).text = areaText
        view.findViewById<TextView>(R.id.tvOwnerLevel).text = levelText

        // Recupera o cosmético atual do dono da zona do SharedPreferences para exibição imersiva
        // Em um app real isso viria da API
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val accessory = prefs.getString("accessory", "none")
        val ivAccessory = view.findViewById<ImageView>(R.id.ivDominatorAccessory)
        
        when (accessory) {
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

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }
}