package com.example.zonex

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zonex.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivitiesFragment : Fragment() {

    private lateinit var rvActivities: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalZones: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_activities, container, false)
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

        rvActivities = view.findViewById(R.id.rvActivities)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance)
        tvTotalZones = view.findViewById(R.id.tvTotalZones)
        
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        
        fetchActivities()
    }

    private fun fetchActivities() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getActivities(userId = 1)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val activities = response.body()!!
                    
                    if (activities.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                        rvActivities.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.GONE
                        rvActivities.visibility = View.VISIBLE
                        rvActivities.adapter = ActivityAdapter(activities)
                        
                        // Atualiza cabeçalho com os dados reais
                        val totalDist = activities.map { it.distanciaMetros }.sum() / 1000f
                        val totalZones = activities.sumOf { it.zonasConquistadas }
                        tvTotalDistance.text = String.format("%.1f km", totalDist)
                        tvTotalZones.text = totalZones.toString()
                    }
                } else {
                    tvEmptyState.visibility = View.VISIBLE
                    tvEmptyState.text = "Erro ao carregar atividades."
                }
            } catch (e: Exception) {
                Log.e("ActivitiesFragment", "Erro API: \${e.message}")
                tvEmptyState.visibility = View.VISIBLE
                tvEmptyState.text = "Sem conexão."
            }
        }
    }
}