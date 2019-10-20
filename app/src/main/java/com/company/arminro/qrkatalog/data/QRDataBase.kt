package com.company.arminro.qrkatalog.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.company.arminro.qrkatalog.helpers.getCurrentDateTimeString
import com.company.arminro.qrkatalog.model.CodeData
import java.util.concurrent.Executors

@Database(entities = arrayOf(CodeData::class), version = 3)
abstract class QRDataBase : RoomDatabase() {
    abstract fun qRDao(): QRDao

    // based on: https://matthiaslischka.at/2019/01/15/Seed-Room-Database/
    companion object {
        @Volatile
        private var INSTANCE: QRDataBase? = null

        fun getInstance(context: Context): QRDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, QRDataBase::class.java, "QR_Database.db")
                //.addCallback(seedDatabaseCallback(context))
                .fallbackToDestructiveMigration()
                .build()

       //private fun seedDatabaseCallback(context: Context): Callback {
       //    return object : Callback() {
       //        override fun onCreate(db: SupportSQLiteDatabase) {
       //            super.onCreate(db)
       //            Executors.newSingleThreadExecutor().execute {
       //                val dao = getInstance(context).qRDao()
       //                //dao.add(CodeData("Sample company", "Sample destination","Sample source", "Sample description", getCurrentDateTimeString()))
       //            }
       //        }
       //    }
       //}
    }
}