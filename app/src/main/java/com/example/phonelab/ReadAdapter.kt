package com.example.phonelab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ReadAdapter:
    RecyclerView.Adapter<ReadAdapter.LecturaViewHolder>() {

    private var lecturas: List<ReadAccelerometer> = emptyList()

    class LecturaViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val texto: TextView = itemView.findViewById(R.id.tvItemLectura)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LecturaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lectura, parent, false)
        return LecturaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: LecturaViewHolder, position: Int) {
        val item = lecturas[position]
        val fecha = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss", Locale.getDefault()
        ).format(Date(item.timestampMs))
        holder.texto.text = String.format(
            Locale.getDefault(),
            "#%d | %s\nX: %.2f  Y: %.2f  Z: %.2f m/s²",
            item.id, fecha, item.x, item.y, item.z
        )
    }

    override fun getItemCount(): Int = lecturas.size

    @Suppress("NotifyDataSetChanged")
    fun actualizar(nuevasLecturas: List<ReadAccelerometer>) {
        lecturas = nuevasLecturas
        notifyDataSetChanged()
    }
}