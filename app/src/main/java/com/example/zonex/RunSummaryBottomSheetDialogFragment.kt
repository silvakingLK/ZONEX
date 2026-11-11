package com.example.zonex

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RunSummaryBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_DISTANCE = "distance"
        private const val ARG_DURATION = "duration"
        private const val ARG_AREA = "area"
        private const val ARG_XP = "xp"

        fun newInstance(distance: String, duration: String, area: String, xp: String): RunSummaryBottomSheetDialogFragment {
            val fragment = RunSummaryBottomSheetDialogFragment()
            val args = Bundle()
            args.putString(ARG_DISTANCE, distance)
            args.putString(ARG_DURATION, duration)
            args.putString(ARG_AREA, area)
            args.putString(ARG_XP, xp)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_run_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fundo transparente para respeitar o shape glassmorphism
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)

        val distance = arguments?.getString(ARG_DISTANCE) ?: "0.00 km"
        val duration = arguments?.getString(ARG_DURATION) ?: "00:00"
        val area = arguments?.getString(ARG_AREA) ?: "+0 m²"
        val xp = arguments?.getString(ARG_XP) ?: "+0 XP"

        view.findViewById<TextView>(R.id.tvSummaryDistance).text = distance
        view.findViewById<TextView>(R.id.tvSummaryDuration).text = duration
        view.findViewById<TextView>(R.id.tvSummaryArea).text = area
        view.findViewById<TextView>(R.id.tvSummaryXp).text = xp

        view.findViewById<AppCompatButton>(R.id.btnContinue).setOnClickListener {
            dismiss()
        }
    }
}