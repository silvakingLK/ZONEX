package com.example.zonex

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SponsorBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_STORE_NAME = "store_name"

        fun newInstance(storeName: String): SponsorBottomSheetDialogFragment {
            val fragment = SponsorBottomSheetDialogFragment()
            val args = Bundle()
            args.putString(ARG_STORE_NAME, storeName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_sponsor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Torna o fundo padrão do BottomSheet transparente para mostrar o shape personalizado
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)

        val storeName = arguments?.getString(ARG_STORE_NAME) ?: "Estabelecimento Parceiro"
        view.findViewById<TextView>(R.id.tvStoreName).text = storeName

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<AppCompatButton>(R.id.btnRedeem).setOnClickListener {
            Toast.makeText(requireContext(), "Cupom resgatado! Capture a área para validar.", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }
}