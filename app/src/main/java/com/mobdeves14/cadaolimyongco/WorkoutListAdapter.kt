package com.mobdeves14.cadaolimyongco

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class WorkoutListAdapter : ListAdapter<Workout, WorkoutListAdapter.WorkoutViewHolder>(WordsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        return WorkoutViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val distancetv: TextView = itemView.findViewById(R.id.distancetv)
        private val durationtv: TextView = itemView.findViewById(R.id.durationtv)
        private val avgspeedtv: TextView = itemView.findViewById(R.id.avgspeedtv)

        fun bind(workout: Workout) {
            distancetv.text = workout.distance.toString()
            durationtv.text = workout.duration.toString()
            avgspeedtv.text = workout.avgSpeed.toString()
        }

        companion object {
            fun create(parent: ViewGroup): WorkoutViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return WorkoutViewHolder(view)
            }
        }
    }

    class WordsComparator : DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem === newItem
        }

        // broken
        override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem.distance == newItem.distance
        }
    }
}
