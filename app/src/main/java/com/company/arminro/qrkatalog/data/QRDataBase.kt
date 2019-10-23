package com.company.arminro.qrkatalog.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.company.arminro.qrkatalog.helpers.TimeConverters
import com.company.arminro.qrkatalog.helpers.getCurrentDateTime
import com.company.arminro.qrkatalog.model.CodeData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.Executors

@Database(entities = arrayOf(CodeData::class), version = 4)
@TypeConverters(TimeConverters::class)
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
                .addCallback(seedDatabaseCallback(context))
                .fallbackToDestructiveMigration()
                .build()

       private fun seedDatabaseCallback(context: Context): Callback {
           return object : Callback() {
               override fun onCreate(db: SupportSQLiteDatabase) {
                   super.onCreate(db)
                        GlobalScope.launch {

                       val dao = getInstance(context).qRDao()
                            dao.add(CodeData("AMPE", "1083 Budapest, Futo st 37","905 SW. Cypress Lane Manchester",
                                "New shipment of potatoes", getCurrentDateTime()))
                            dao.add(CodeData("MyAwesomeCompany", "Hungary","Canada",
                                "Facial recognition software solution with neural networks", DateTime(2018, 12, 21, 10, 10, 10).toDate()))
                            dao.add(CodeData("Omnica Corporation", "Hungary","Detroit",
                                "Shipment of not at all odd looking robot things", getCurrentDateTime()))
                            dao.add(CodeData("N7", "[unspecified]","Palaven",
                                "Specter status not recognised", DateTime(2165, 4, 11, 12, 12, 12).toDate()))

                   }
               }
           }
       }
    }
}