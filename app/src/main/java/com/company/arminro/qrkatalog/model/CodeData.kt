package com.company.arminro.qrkatalog.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

@Entity
data class CodeData(
    @ColumnInfo(name = "company") var companyName: String,
    @ColumnInfo(name = "dest") var destination: String,
    var source: String,
    var description: String) {

        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
        var timestampCreated: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    }