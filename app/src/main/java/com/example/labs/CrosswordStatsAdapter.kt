package com.example.labs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CrosswordStatsAdapter : ListAdapter<CrosswordStats, CrosswordStatsAdapter.StatsViewHolder>(StatsDiffCallback) {

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val difficultyTextView: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val timeTextView: TextView = itemView.findViewById(R.id.tvTime)
        private val scoreTextView: TextView = itemView.findViewById(R.id.tvScore)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(stat: CrosswordStats) {
            difficultyTextView.text = when (stat.difficulty) {
                "Легкий" -> "🟢 Легкий"
                "Средний" -> "🟡 Средний"
                "Сложный" -> "🔴 Сложный"
                else -> stat.difficulty
            }

            val minutes = stat.completionTime / 60000
            val seconds = (stat.completionTime % 60000) / 1000
            timeTextView.text = String.format("%02d:%02d", minutes, seconds)

            scoreTextView.text = "${stat.score}%"
            scoreTextView.setTextColor(
                when {
                    stat.score >= 90 -> 0xFF4CAF50.toInt()
                    stat.score >= 70 -> 0xFFFF9800.toInt()
                    else -> 0xFFF44336.toInt()
                }
            )

            val date = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(stat.dateCompleted))
            dateTextView.text = date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crossword_stat, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val stat = getItem(position)
        holder.bind(stat)
    }

    companion object StatsDiffCallback : DiffUtil.ItemCallback<CrosswordStats>() {
        override fun areItemsTheSame(oldItem: CrosswordStats, newItem: CrosswordStats): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CrosswordStats, newItem: CrosswordStats): Boolean {
            return oldItem == newItem
        }
    }
}