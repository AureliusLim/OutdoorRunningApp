package com.mobdeves14.cadaolimyongco
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DataGenerator {
    companion object {
        fun loadData(): ArrayList<WorkoutModel> {
            val calendar = Calendar.getInstance()
            val currentDate = calendar.time
            val data = ArrayList<WorkoutModel>()



            data.add(WorkoutModel(23, 55, 6.9, 120, 800, "Saturday", "19", "Thursday, Oct 19 2023"))
            data.add(WorkoutModel(14, 11, 9.0, 180, 550, "Saturday", "20", "Friday, Oct 20 2023"))
            data.add(WorkoutModel(11, 22, 8.9, 170, 342, "Saturday", "21", "Saturday, Oct 21 2023"))
            data.add(WorkoutModel(23, 33, 7.8, 170, 690, "Saturday",  "22", "Sunday Oct 22 2023"))
            data.add(WorkoutModel(22, 44, 6.5, 150, 890, "Saturday", "23", "Monday, Oct 23 2023"))

            return data
        }
    }
}
