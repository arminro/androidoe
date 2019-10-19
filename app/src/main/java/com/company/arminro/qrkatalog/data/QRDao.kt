package com.company.arminro.qrkatalog.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.company.arminro.qrkatalog.model.CodeData
import java.sql.Timestamp
import java.util.*

@Dao
interface QRDao {
    @Query("SELECT * FROM codedata")
    suspend fun getAll(): LiveData<List<CodeData>>

    // interested in all the result where the destination appears in any form
    // interestingly this is not enough: https://stackoverflow.com/questions/44184769/android-room-select-query-with-like
    @Query("SELECT * FROM codedata where codedata.company like :companyName")
    suspend fun getAllByCompany(companyName: String): LiveData<List<CodeData>>

    @Query("SELECT * FROM codedata where codedata.dest like :destination")
    suspend fun getAllTo(destination: String): LiveData<List<CodeData>>

    @Query("SELECT * FROM codedata where codedata.timestampCreated > :date")
    suspend fun getAllAfter(date: String): LiveData<List<CodeData>>

    @Query("SELECT * FROM codedata where codedata.timestampCreated < :date")
    suspend fun getAllBefore(date: String): LiveData<List<CodeData>>

    @Query("SELECT * FROM codedata where codedata.timestampCreated > :dateStart and codedata.timestampCreated < :dateEnd")
    suspend fun getAllBetween(dateStart: String, dateEnd: String): LiveData<List<CodeData>>

    @Query("SELECT * FROM codedata where codedata.source like :source")
    suspend fun getAllFrom(source: String): LiveData<List<CodeData>>

    @Query("SELECT * FROM codedata where codedata.id = :id")
    suspend fun getById(id: Long): LiveData<CodeData>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun add(dataToAdd: CodeData)

    @Delete
    suspend fun delete(dataToDelete: CodeData)

    @Update
    suspend fun update(newData: CodeData)
}