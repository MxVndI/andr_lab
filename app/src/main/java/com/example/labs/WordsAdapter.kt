package com.example.labs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class WordsAdapter : ListAdapter<CrosswordWord, WordsAdapter.WordViewHolder>(DiffCallback) {

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberTextView: TextView = itemView.findViewById(R.id.tvWordNumber)
        private val clueTextView: TextView = itemView.findViewById(R.id.tvWordClue)
        private val lengthTextView: TextView = itemView.findViewById(R.id.tvWordLength)

        fun bind(word: CrosswordWord) {
            numberTextView.text = "${word.number}."
            clueTextView.text = word.clue
            lengthTextView.text = "${word.word.length} букв"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = getItem(position)
        holder.bind(word)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CrosswordWord>() {
        override fun areItemsTheSame(oldItem: CrosswordWord, newItem: CrosswordWord): Boolean {
            return oldItem.word == newItem.word && oldItem.direction == newItem.direction
        }

        override fun areContentsTheSame(oldItem: CrosswordWord, newItem: CrosswordWord): Boolean {
            return oldItem == newItem
        }
    }
}