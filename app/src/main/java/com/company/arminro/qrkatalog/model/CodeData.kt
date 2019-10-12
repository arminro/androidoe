package com.company.arminro.qrkatalog.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
data class CodeData(
    @PrimaryKey(autoGenerate = true) val id: UUID,
    @ColumnInfo(name = "company") var companyName: String,
    @ColumnInfo(name = "dest") var destination: String,
    var source: String
)