package com.mobdeves14.cadaolimyongco

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProgressActivity: AppCompatActivity(), SelectListener {
    private lateinit var runTab:ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutDistance: TextView
    private lateinit var workoutDuration: TextView
    private lateinit var workoutCalories: TextView
    private lateinit var workoutHeart: TextView
    private lateinit var workoutSpeed: TextView
    private lateinit var actualDate: TextView
    private val workoutList: ArrayList<WorkoutModel> = DataGenerator.loadData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        this.runTab = findViewById(R.id.runtab)
        this.recyclerView = findViewById(R.id.dates)
        this.workoutDistance = findViewById(R.id.workoutdistance)
        this.workoutDuration = findViewById(R.id.workoutduration)
        this.workoutCalories = findViewById(R.id.workoutcalories)
        this.workoutHeart = findViewById(R.id.workoutheart)
        this.workoutSpeed = findViewById(R.id.workoutspeed)
        this.actualDate = findViewById(R.id.actualdate)

        this.workoutDuration.text = "Duration \n\n" + workoutList[0].duration.toString()
        this.workoutCalories.text = "Calories \n\n" + workoutList[0].calories.toString()
        this.workoutHeart.text = "Heart Rate \n\n" + workoutList[0].heartRate.toString()
        this.workoutDistance.text = workoutList[0].distance.toString() + "km"
        this.workoutSpeed.text = "Avg Speed \n\n" + workoutList[0].avgSpeed.toString()
        this.actualDate.text = workoutList[0].actualdate
        this.recyclerView.adapter = WorkoutAdapter(this.workoutList, this)

        this.recyclerView.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)



        runTab.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClicked(workoutView: WorkoutModel) {
        this.workoutDuration.text = "Duration \n\n" + workoutView.duration.toString()
        this.workoutCalories.text = "Calories \n\n" + workoutView.calories.toString()
        this.workoutHeart.text = "Heart Rate \n\n" + workoutView.heartRate.toString()
        this.workoutDistance.text = workoutView.distance.toString() + "km"
        this.workoutSpeed.text = "Avg Speed \n\n" + workoutView.avgSpeed.toString()
        this.actualDate.text = workoutView.actualdate
    }
}