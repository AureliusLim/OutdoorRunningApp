package com.mobdeves14.cadaolimyongco

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
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
    private lateinit var homeTab: ImageButton
    private val workoutList: ArrayList<WorkoutModel> = DataGenerator.loadData()

    private val workoutViewModel: WorkoutViewModel by viewModels {
        WorkoutViewModelFactory((application as WorkoutApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        this.runTab = findViewById(R.id.runtab)
        this.homeTab = findViewById(R.id.home_btn)
        this.recyclerView = findViewById(R.id.dates)
        this.workoutDistance = findViewById(R.id.workoutdistance)
        this.workoutDuration = findViewById(R.id.duration_min11)
        this.workoutCalories = findViewById(R.id.calories_tv)
        this.workoutHeart = findViewById(R.id.avg_speed_tv2)
        this.workoutSpeed = findViewById(R.id.avg_speed_tv)
        this.actualDate = findViewById(R.id.actualdate)

        this.workoutDuration.text =  workoutList[0].duration.toString()
        this.workoutCalories.text =  workoutList[0].calories.toString()
        this.workoutHeart.text =  workoutList[0].heartRate.toString()
        this.workoutDistance.text = workoutList[0].distance.toString()
        this.workoutSpeed.text = workoutList[0].avgSpeed.toString()
        this.actualDate.text = workoutList[0].actualdate
        val adapter = WorkoutListAdapter()
        this.recyclerView.adapter = adapter

        this.recyclerView.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        workoutViewModel.allWorkouts.observe(this) { workouts ->
            // Update the cached copy of the words in the adapter.
            workouts.let { adapter.submitList(it) }
        }


        homeTab.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        runTab.setOnClickListener {
            val intent = Intent(this, RunActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    override fun onItemClicked(workoutView: WorkoutModel) {
        this.workoutDuration.text = workoutView.duration.toString()
        this.workoutCalories.text = workoutView.calories.toString()
        this.workoutHeart.text = workoutView.heartRate.toString()
        this.workoutDistance.text = workoutView.distance.toString()
        this.workoutSpeed.text = workoutView.avgSpeed.toString()
        this.actualDate.text = workoutView.actualdate
    }
}