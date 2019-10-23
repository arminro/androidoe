package com.company.arminro.qrkatalog.helpers

import java.text.SimpleDateFormat
import java.util.*
import android.R.id.edit
import androidx.room.Room
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.data.QRDao_Impl
import com.company.arminro.qrkatalog.data.QRDataBase


// full credit: https://stackoverflow.com/questions/47006254/how-to-get-current-local-date-and-time-in-kotlin
// package lvl functions
fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}

fun getCurrentDateTimeString() : String{
    return getCurrentDateTime().toString()
}

fun saveDataToSharedPReferences(cont: Context, settingName: String, value: Boolean) {
    // based on: https://stackoverflow.com/questions/15385117/save-little-information-as-setting-in-android-like-first-time-that-app-is-run
    // using shared prefs is cool because it is deleted when the user deletes the app

    val editor =  PreferenceManager.getDefaultSharedPreferences(cont).edit()
    editor.putBoolean(settingName, value)
    editor.commit()
}

fun loadDataFromSharedPreferences(cont: Context, settingName: String) : Boolean?{
    return cont.getSharedPreferences(settingName, 0)
        .getBoolean(settingName, false)
}


