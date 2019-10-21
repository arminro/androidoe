package com.company.arminro.qrkatalog.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import android.provider.SyncStateContract
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.properties.Delegates

// for reasons of simplicity this class is both a data model (entity) and the view model (parcelable) class
@Entity
@Parcelize
data class CodeData(
    @ColumnInfo(name = "company") var companyName: String,
    @ColumnInfo(name = "dest") var destination: String,
    var source: String,
    var description: String,
    var timestampCreated: String,
    @PrimaryKey(autoGenerate = true) var id: Long = 0L) : Parcelable






