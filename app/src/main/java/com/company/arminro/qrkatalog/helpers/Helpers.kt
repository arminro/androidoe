package com.company.arminro.qrkatalog.viewhelpers

import java.text.SimpleDateFormat
import java.util.*

// full credit: https://stackoverflow.com/questions/47006254/how-to-get-current-local-date-and-time-in-kotlin
fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}