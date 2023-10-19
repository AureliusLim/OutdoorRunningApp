package com.mobdeves14.cadaolimyongco

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class WorkoutView(itemView: View): RecyclerView.ViewHolder(itemView) {


    val dayofmonth: TextView = itemView.findViewById(R.id.dayofmonth)
    val dayofweek: TextView = itemView.findViewById(R.id.dayofweek)
    val containerday: LinearLayout= itemView.findViewById(R.id.containerday)


    fun bindData(Workout : WorkoutModel){

        dayofmonth.text = Workout.dayofmonth;
        dayofweek.text = Workout.dayofweek;


    }


}