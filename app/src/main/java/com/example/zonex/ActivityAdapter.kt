package com.example.zonex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zonex.network.ActivityDto

class ActivityAdapter(private val activities: List<ActivityDto>) :
    RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]
        holder.tvType.text = activity.tipo
        holder.tvDate.text = activity.data
        holder.tvDistance.text = String.format("%.2f km", activity.distanciaMetros / 1000f)
        
        val minutes = activity.duracaoSegundos / 60
        val seconds = activity.duracaoSegundos % 60
        holder.tvDuration.text = String.format("%02d:%02d", minutes, seconds)
        
        if (activity.tipo.equals("Caminhada", ignoreCase = true)) {
            holder.ivIcon.setImageResource(R.drawable.ic_walk)
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_run)
        }
    }

    override fun getItemCount() = activities.size
}