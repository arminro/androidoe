package com.company.arminro.qrkatalog.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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
    var timestampCreated: String) : Parcelable{

    @PrimaryKey(autoGenerate = true) var id: Long = 0
}



