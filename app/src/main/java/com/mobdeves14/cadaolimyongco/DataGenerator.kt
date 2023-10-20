package com.mobdeves14.cadaolimyongco
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DataGenerator {
    companion object {
        fun loadData(): ArrayList<WorkoutModel> {
            val calendar = Calendar.getInstance()
            val listofmonthday = ArrayList<String>()
            val listofweekday= ArrayList<String>()
            val listofDates= ArrayList<String>()
            val dateFormatFullMonth = SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US)
            val data = ArrayList<WorkoutModel>()
            for(i in 0 until 5){

                val currentDate = calendar.time
                val formattedDateFullMonth = dateFormatFullMonth.format(currentDate)
                listofDates.add(formattedDateFullMonth)
                listofmonthday.add( SimpleDateFormat("d", Locale.US).format(currentDate))
                listofweekday.add(SimpleDateFormat("E", Locale.US).format(currentDate))
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }


            data.add(WorkoutModel(23, 55, 6.9, 120, 800, listofweekday[0], listofmonthday[0], listofDates[0]))
            data.add(WorkoutModel(14, 11, 9.0, 180, 550, listofweekday[1], listofmonthday[1], listofDates[1]))
            data.add(WorkoutModel(11, 22, 8.9, 170, 342, listofweekday[2], listofmonthday[2], listofDates[2]))
            data.add(WorkoutModel(23, 33, 7.8, 170, 690, listofweekday[3],  listofmonthday[3], listofDates[3]))
            data.add(WorkoutModel(22, 44, 6.5, 150, 890, listofweekday[4], listofmonthday[4], listofDates[4]))

            return data
        }
    }
}
