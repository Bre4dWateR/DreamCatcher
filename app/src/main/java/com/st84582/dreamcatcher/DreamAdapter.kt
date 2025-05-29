package com.st84582.dreamcatcher

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st84582.dreamcatcher.data.model.Dream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DreamAdapter(
    private var dreams: List<Dream>,
    private val onItemClick: (Dream) -> Unit
) : RecyclerView.Adapter<DreamAdapter.DreamViewHolder>() {

    class DreamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.itemDreamTitle)
        val dateTextView: TextView = itemView.findViewById(R.id.itemDreamDate)
        val descriptionTextView: TextView = itemView.findViewById(R.id.itemDreamDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dream, parent, false)
        return DreamViewHolder(view)
    }

    override fun onBindViewHolder(holder: DreamViewHolder, position: Int) {
        val dream = dreams[position]
        holder.titleTextView.text = dream.title
        holder.descriptionTextView.text = dream.story
        holder.itemView.setOnClickListener { onItemClick(dream) }

        if (dream.date.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val parsedDate = LocalDate.parse(dream.date, DateTimeFormatter.ISO_LOCAL_DATE)
                    val desiredFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                    holder.dateTextView.text = parsedDate.format(desiredFormatter)
                } catch (e: Exception) {
                    holder.dateTextView.text = dream.date
                }
            } else {
                try {
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = inputFormat.parse(dream.date)
                    if (date != null) {
                        holder.dateTextView.text = outputFormat.format(date)
                    } else {
                        holder.dateTextView.text = dream.date
                    }
                } catch (e: Exception) {
                    holder.dateTextView.text = dream.date
                }
            }
        } else {
            holder.dateTextView.text = "No date!"
        }
    }

    override fun getItemCount(): Int = dreams.size

    fun updateDreams(newDreams: List<Dream>) {
        dreams = newDreams
        notifyDataSetChanged()
    }
}

