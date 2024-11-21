package com.example.maplab

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private val list: MutableList<PStudent>,
    private val viewMoreClicked: (PStudent) -> Unit
) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idView: TextView = itemView.findViewById(R.id.sid)
        val minView: TextView = itemView.findViewById(R.id.tvMinSpeed)
        val maxView: TextView = itemView.findViewById(R.id.maxSpeedText)
        val btnView: Button = itemView.findViewById(R.id.viewMoreBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_publisher, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val publisher = list[position]
        val textMin = "min speed: " + String.format("%.1f", publisher.minSpeed) + " km/h"
        val textMax = "max speed: " + String.format("%.1f", publisher.maxSpeed) + " km/h"

        holder.idView.text = publisher.studentID
        holder.minView.text = textMin
        holder.maxView.text = textMax
        holder.btnView.setOnClickListener {
            viewMoreClicked(publisher)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePublisherList(newPublisherList: MutableList<PStudent>) {
        list.clear()
        list.addAll(newPublisherList)
        notifyDataSetChanged()
    }
}