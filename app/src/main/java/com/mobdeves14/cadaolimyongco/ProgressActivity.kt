package com.mobdeves14.cadaolimyongco

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProgressActivity: AppCompatActivity(), SelectListener {
    private lateinit var runTab:ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutDistance: TextView
    private lateinit var workoutDuration: TextView
    private lateinit var workoutCalories: TextView
    private lateinit var pace: TextView
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
        this.pace = findViewById(R.id.avg_speed_tv2)

        val adapter = WorkoutListAdapter(this)

        this.recyclerView.adapter = adapter

        this.recyclerView.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


//        workoutViewModel.allWorkouts.observe(this) { workouts ->
//            // Update the cached copy of the words in the adapter.
//            workouts.let { adapter.submitList(it) }
//        }
        workoutViewModel.filteredWorkouts.observe(this) { filteredWorkouts ->
            // Update the adapter with the filtered data
            if(filteredWorkouts.isNotEmpty()){
                this.workoutDuration.text =  formatTime(filteredWorkouts[0].duration)
                this.workoutCalories.text =  filteredWorkouts[0].calories.toString()
                this.workoutHeart.text =  filteredWorkouts[0].pace.toString()
                this.workoutDistance.text = filteredWorkouts[0].distance.toString()
                this.workoutSpeed.text = filteredWorkouts[0].avgSpeed.toString()
                var parsedDate= stringToDate(filteredWorkouts[0].date)
                var targetFormat = SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US)
                this.actualDate.text = targetFormat.format(parsedDate)
                adapter.submitList(filteredWorkouts)
            }
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClicked(workoutView: Workout) {
        var curr = workoutView.date
        Log.d("retrievedDate", "$curr")
        var parsedDate= stringToDate(workoutView.date)

        var targetFormat = SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US)
        this.workoutDuration.text = formatTime(workoutView.duration)
        this.workoutCalories.text = workoutView.calories.toString()
        this.workoutDistance.text = workoutView.distance.toString()
        this.workoutSpeed.text = String.format("%.2f", workoutView.avgSpeed)
        this.actualDate.text = targetFormat.format(parsedDate)
        this.pace.text = formatTime(workoutView.pace)

    }
    private fun formatTime(seconds: Long): String {
        val minutes = (seconds / 60).toInt()
        val remainingSeconds = (seconds % 60).toInt()
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }


    private fun stringToDate(originalDate: String): Date {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val parsedDate = originalFormat.parse(originalDate)
        return parsedDate
    }
}