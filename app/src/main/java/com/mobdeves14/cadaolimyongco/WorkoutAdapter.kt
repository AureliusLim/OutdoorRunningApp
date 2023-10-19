package com.mobdeves14.cadaolimyongco

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter


class WorkoutAdapter(private val data: ArrayList<WorkoutModel>, private val listener:SelectListener): Adapter<WorkoutView>() {
    /*  onCreateViewHolder requires in two parameters:
            parent -> Which is the parent View where this adapter is associated with; this is
                      typically the RecyclerView
                      recall: recyclerView.adapter = MyAdapter(this.characterList)
            viewType -> This parameter refers to the
    * */
    private var clickedPos = -1


    private lateinit var onItemClick: (Int) -> Unit // Callback to handle click

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutView {
        // Create a LayoutInflater using the parent's (i.e. RecyclerView's) context
        val inflater = LayoutInflater.from(parent.context)
        // Inflate a new View given the item_layout.xml item view we created.
        val view = inflater.inflate(R.layout.workout_view, parent, false)
        // Return a new instance of our MyViewHolder passing the View object we created
        return WorkoutView(view)
    }

    /*  Whenever the RecyclerView feels the need to bind data, onBindViewHolder is called. Here, we
        gain access to the specific ViewHolder instance and the position in our data that we should
        be binding to the view.
    * */
    override fun onBindViewHolder(holder: WorkoutView, position: Int) {
        // Please note that bindData is a function we created to adhere to encapsulation. There are
        // many ways to implement the binding of data.
        val currentItem = data[position]
        holder.bindData(data.get(position))
        holder.containerday.setOnClickListener(View.OnClickListener { // When you're inside the click listener interface,
            // you can access the position using the ViewHolder.
            // We'll store the position in the member variable in this case.
            clickedPos = holder.adapterPosition
            Log.d("ADAPTER POSITION", "$clickedPos")
            listener.onItemClicked(currentItem)
        })

    }




    override fun getItemCount(): Int{
        return data.size
    }
}