package com.src.uscan.utils

import android.util.Log
import java.text.DateFormat
import java.text.Format
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    /**
     * time format==Wed Apr 15 21:17:08 GMT+05:30 2020
     */


    fun parseDateToddMMyyyy(time: String?, inputFormat:String, outFormat:String): String? {
        val inputFormat = SimpleDateFormat(inputFormat)
        val outputFormat = SimpleDateFormat(outFormat)
        var date: Date?
        var str: String? = null
        try {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        } catch (e: ParseException) {
            Log.e("MEssage",""+e.printStackTrace())
        }
        return str
    }

    /**
     *convert string to date
     */
    fun getDateFromString(date:String): Date? {
        var sdf =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(date);
        } catch ( ex:ParseException) {
            Log.v("Exception", ex.localizedMessage);
        }
        return Date()
    }


    /**
     * get current time
     */

    fun getCurrentTime(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"))
        val currentLocalTime = cal.time
        val date: DateFormat = SimpleDateFormat("yyyy-mm-dd HH:mm:ss")
        date.timeZone = TimeZone.getTimeZone("GMT+5:30")
        Log.i("Time","sent"+date.toString())
        return date.format(currentLocalTime)
    }

    fun convertTime(time: Long): String? {
        val date = Date(time)
        val format: Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(date)
    }

}
