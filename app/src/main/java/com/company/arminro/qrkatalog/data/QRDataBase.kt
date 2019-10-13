package com.company.arminro.qrkatalog.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.company.arminro.qrkatalog.model.CodeData

@Database(entities = arrayOf(CodeData::class), version = 1)
abstract class QRDataBase : RoomDatabase() {
    abstract fun QRDao(): QRDao
}