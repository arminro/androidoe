package com.company.arminro.qrkatalog.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.company.arminro.qrkatalog.model.CodeData
import java.sql.Timestamp
import java.util.*

@Dao
interface QRDao {
    @Query("SELECT * FROM codedata")
    fun getAll(): List<CodeData>

    // interested in all the result where the destination appears in any form
    // interestingly this is not enough: https://stackoverflow.com/questions/44184769/android-room-select-query-with-like
    @Query("SELECT * FROM codedata where codedata.company like :companyName")
    suspend fun getAllByCompany(companyName: String): List<CodeData>

    @Query("SELECT * FROM codedata where codedata.dest like :destination")
    fun getAllTo(destination: String): List<CodeData>

    @Query("SELECT * FROM codedata where codedata.timestampCreated > :date")
    fun getAllAfter(date: String): List<CodeData>

    @Query("SELECT * FROM codedata where codedata.timestampCreated < :date")
    fun getAllBefore(date: String): List<CodeData>

    @Query("SELECT * FROM codedata where codedata.timestampCreated > :dateStart and codedata.timestampCreated < :dateEnd")
    fun getAllBetween(dateStart: String, dateEnd: String): List<CodeData>

    @Query("SELECT * FROM codedata where codedata.source like :source")
    fun getAllFrom(source: String): List<CodeData>

    @Query("SELECT * FROM codedata where codedata.id = :id")
    fun getById(id: Long): CodeData

    @Insert
    suspend fun add(dataToAdd: CodeData)

    @Delete
    fun delete(dataToDelete: CodeData)

    @Update
    suspend fun update(newData: CodeData)


}