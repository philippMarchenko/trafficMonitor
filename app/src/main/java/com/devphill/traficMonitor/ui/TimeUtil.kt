package com.devphill.traficMonitor.ui

import android.content.Context
import android.text.format.DateFormat
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimeUtil{

    companion object {

        const val LOG_TAG = "TimeUtilLog"

        fun getDateFromLists(readDate: String): String {

            val result: String


            val format =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            format.timeZone = TimeZone.getTimeZone("UTC") // missing line
            val past = format.parse(readDate)
            val now =  Date()

            val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
            TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
            val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
            val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)
            val weeks = days / 7
            val month = weeks / 4
            val years = month / 12

            result = when {
                seconds < 59 -> "Less than a minute ago"
                /*     else if(minutes <= 59 ){
                             result = "Less than $minutes minutes ago"
                         }*/
                hours <= 1 -> "Less than an hour ago"
                hours <= 23 -> "Less than $hours hours ago"
                days < 7 -> "Less than $days days ago"
                weeks < 4 -> "Less than $weeks weeks ago"
                month < 12 -> "Less than $month month ago"
                else -> "Less than $years years ago"
            }

            return result

        }


        fun getTimeMsFromServerTime(date: String): Long {

            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            readDate.timeZone = TimeZone.getTimeZone("UTC") // missing line
            val dateFormated = readDate.parse(date)

            return dateFormated.time

        }

        fun getMsByFormat(format: String, date: String): Long {

            val readDate = SimpleDateFormat(format, Locale.ENGLISH)
            readDate.timeZone = TimeZone.getTimeZone("UTC") // missing line
            val dateFormated = readDate.parse(date)

            return dateFormated.time
        }

        fun getTimeMsFromDate(date: Date): Long {
            return date.time
        }


        fun getDateByFormatWhithoutTimezone(formatOut: String, date: String): String {

            var res = ""

            try{
                val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
                readDate.timeZone = TimeZone.getTimeZone("UTC") // missing line
                val dateFormated = readDate.parse(date)

                val timeFormat = SimpleDateFormat(formatOut, Locale.ENGLISH)

                res = timeFormat.format(dateFormated)
            }catch (e: java.lang.Exception){

            }
            return res
        }

        fun isDatePassed(date: Date): Boolean {
            val currentDate = Date(System.currentTimeMillis())
            return date.before(currentDate)
        }

        fun getTodayByFormat(format: String): String {

            val readDate = SimpleDateFormat(format, Locale.ENGLISH)
            val today = Date(System.currentTimeMillis())

            return readDate.format(today)
        }

        fun addMinutesToTime12h(minutes: Int, time: String): String{

            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val d = df.parse(time)
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.MINUTE, minutes)

            return df.format(cal.time)
        }

        private fun getDateFromServerType(date: String): Date {

            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            return readDate.parse(date)
        }


        fun getDateFromTaskDetails(date: String,context: Context): String {

            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            readDate.timeZone = TimeZone.getTimeZone("UTC") // missing line
            val dateFormated = readDate.parse(date)

            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
            val year = yearFormat.format(dateFormated)
            val monthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
            val month = monthFormat.format(dateFormated)
            val dayInMonthFormat = SimpleDateFormat("d", Locale.ENGLISH)
            val dayInMonth = dayInMonthFormat.format(dateFormated)


            var timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

            if(DateFormat.is24HourFormat(context)) {
                timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            }

            val time = timeFormat.format(dateFormated)

            return "$month $dayInMonth, $year at $time"

        }

        fun getHoursAndMinutesFromMinutes(mins: Int): String {

            val hours = mins / 60
            val minutes = mins % 60

            return when {
                hours == 0 -> "$minutes minutes"
                minutes == 0 -> "$hours hours"
                else -> "$hours hours, $minutes minutes"
            }
        }

        fun getDaysBetweenDates(startdate: Date, enddate: Date): ArrayList<Date> {
            val dates = ArrayList<Date>()

            val interval = (24 * 1000 * 60 * 60).toLong() // 1 hour in millis
            val endTime = enddate.time // create your endtime here, possibly using Calendar or Date
            var curTime = startdate.time
            while (curTime <= endTime) {
                dates.add(Date(curTime))
                curTime += interval
            }
            return dates
        }

        fun msToServerFormat(ms: Long): String{
            val date = Date(ms)
            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            readDate.timeZone = TimeZone.getTimeZone("UTC") // missing line
            return readDate.format(date)
        }

        fun msToFormat(ms: Long,format: String): String{

            val date = Date(ms)

            val readDate = SimpleDateFormat(format, Locale.ENGLISH)
            readDate.timeZone = TimeZone.getTimeZone("UTC") // missing line

            return readDate.format(date)
        }

        fun msToServerFormatCompleteTask(ms: Long): String{

            val date = Date(ms)

            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)

            return readDate.format(date)
        }

        fun convertFormatToFormat(formatIn: String,formatOut: String,date: String): String {
            var outString= ""

            try{
                val readDate = SimpleDateFormat(formatIn, Locale.ENGLISH)
                readDate.timeZone = TimeZone.getTimeZone("GMT")
                val dateFormated = readDate.parse(date)

                val timeFormat = SimpleDateFormat(formatOut, Locale.ENGLISH)
                val time = timeFormat.format(dateFormated)

                outString = time

            }catch (e: java.lang.Exception){ Timber.e(e) }

            return outString
        }

        fun convertFormatToFormatWithoutAnyTimezones(formatIn: String,formatOut: String,date: String?): String {

            val readDate = SimpleDateFormat(formatIn, Locale.ENGLISH)
            val dateFormated = readDate.parse(date)

            val timeFormat = SimpleDateFormat(formatOut, Locale.ENGLISH)
            return timeFormat.format(dateFormated)

        }


        fun convertFormatToFormatWithTimezone(formatIn: String, formatOut: String, date: String, timezoneIn: TimeZone?, timezoneOut: TimeZone?): String {
            var time = ""

            try{
                val readDate = SimpleDateFormat(formatIn, Locale.US)
                if(timezoneIn != null){
                    readDate.timeZone = timezoneIn
                }
                val dateFormated = readDate.parse(date)

                val timeFormat = SimpleDateFormat(formatOut , Locale.US)
                if(timezoneOut != null){
                    timeFormat.timeZone = timezoneOut
                }
                time = timeFormat.format(dateFormated)
            }catch (e: Exception){}

            return time
        }

        fun guestSheduleGetDay(date: String): Int{
            val format =  SimpleDateFormat("MMM dd,yyyy", Locale.US)
            val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
            return dayFormat.format(format.parse(date)).toInt()
        }

        fun guestSheduleGetMonth(date: String): Int{
            val format =  SimpleDateFormat("MMM dd,yyyy", Locale.US)
            val dayFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            return dayFormat.format(format.parse(date)).toInt() - 1
        }

        fun guestSheduleGetYear(date: String): Int{
            val format =  SimpleDateFormat("MMM dd,yyyy", Locale.US)
            val dayFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
            return dayFormat.format(format.parse(date)).toInt()
        }

        fun compareDates(startDay: Int, startMonth: Int, startYear: Int, endDay: Int, endMonth: Int, endYear: Int): Boolean{
            val dateStart = Calendar.getInstance()
            val dateEnd = Calendar.getInstance()
            dateStart.set(startYear,startMonth,startDay)
            dateEnd.set(endYear,endMonth,endDay)
            return (dateStart <= dateEnd)
        }

        fun getDaysAfterDate(date: String?): Int {

            val currentDate = Date(System.currentTimeMillis())

            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            readDate.timeZone = TimeZone.getTimeZone("UTC") // missing line
            val dateFormatted = readDate.parse(date)

            val diff = currentDate.time - dateFormatted.time

            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()

        }

        fun getDateFromString(date: String): Date {

            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)

            return readDate.parse(date)
        }

        fun getStringDateFromDate(date: Date): String {

            val readDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

            return readDate.format(date)
        }

        fun getDifferenceMsBetweenToDate(date1: Date, date2: Date): Long {

            val cal1 = Calendar.getInstance()
            val cal12 = Calendar.getInstance()
            cal1.time = date1
            cal12.time = date2

            return cal1.timeInMillis - cal12.timeInMillis
        }

        fun getTimeWhithoutServerTimezone(date: String, applicationContext: Context?): String? {

            val readDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val dateFormated = readDate.parse(date)

            var timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

            if(DateFormat.is24HourFormat(applicationContext)) {
                timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            }

            return timeFormat.format(dateFormated)

        }

        fun getDayOfWeek(value: Int): String {
            var day = ""
            when (value) {
                1 -> day = "Monday"
                2 -> day = "Tuesday"
                3 -> day = "Wednesday"
                4 -> day = "Thursday"
                5 -> day = "Friday"
                6 -> day = "Saturday"
                7 -> day = "Sunday"

            }
            return day
        }


        fun getTimePostfix(context: Context?): String = if(DateFormat.is24HourFormat(context)) {
            "HH:mm"
        } else{
            "hh:mm a"
        }

        fun getTimeFromMs(ms: Long,format: String): String {
            val currentDate = Date(ms)

            val format = SimpleDateFormat(format, Locale.ENGLISH)

            return format.format(currentDate.time)
        }

    }
}