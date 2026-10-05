package uz.farzandim.parent.util

import java.text.SimpleDateFormat
import java.util.*

object DiaryDateHelper {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayMonthFormat = SimpleDateFormat("d-MMMM", Locale("uz"))
    private val monthFormat = SimpleDateFormat("MMMM", Locale("uz"))

    private val uzDayNames = arrayOf(
        "Yakshanba",
        "Dushanba",
        "Seshanba",
        "Chorshanba",
        "Payshanba",
        "Juma",
        "Shanba"
    )

    fun getMondayOfDate(calendar: Calendar): Calendar {
        val cal = calendar.clone() as Calendar
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // In Calendar: Sunday=1, Monday=2, Tuesday=3, ... Saturday=7
        val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
        cal.add(Calendar.DAY_OF_MONTH, diff)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun toIsoDateStr(calendar: Calendar): String {
        return isoFormat.format(calendar.time)
    }

    fun parseIsoDate(dateStr: String?): Calendar {
        val cal = Calendar.getInstance()
        if (dateStr.isNullOrBlank()) return cal
        try {
            val clean = dateStr.split("T").firstOrNull()?.take(10) ?: return cal
            val date = isoFormat.parse(clean)
            if (date != null) cal.time = date
        } catch (e: Exception) {
            // fallback
        }
        return cal
    }

    fun getWeekDays(mondayCal: Calendar): List<Calendar> {
        val days = mutableListOf<Calendar>()
        for (i in 0 until 6) { // Monday to Saturday
            val c = mondayCal.clone() as Calendar
            c.add(Calendar.DAY_OF_MONTH, i)
            days.add(c)
        }
        return days
    }

    fun formatWeekRange(mondayCal: Calendar): String {
        val saturdayCal = mondayCal.clone() as Calendar
        saturdayCal.add(Calendar.DAY_OF_MONTH, 5)

        val monDay = mondayCal.get(Calendar.DAY_OF_MONTH)
        val satDay = saturdayCal.get(Calendar.DAY_OF_MONTH)
        val monMonth = monthFormat.format(mondayCal.time)
        val satMonth = monthFormat.format(saturdayCal.time)

        return if (monMonth == satMonth) {
            "$monDay - $satDay $monMonth"
        } else {
            "$monDay $monMonth - $satDay $satMonth"
        }
    }

    fun formatDayLabel(calendar: Calendar): String {
        val dayIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val dayName = if (dayIdx in uzDayNames.indices) uzDayNames[dayIdx] else ""
        val dayMonth = dayMonthFormat.format(calendar.time)
        return "$dayName, $dayMonth"
    }
}
