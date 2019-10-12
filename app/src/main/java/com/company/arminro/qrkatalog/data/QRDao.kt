package com.company.arminro.qrkatalog.data

import android.arch.persistence.room.*
import com.company.arminro.qrkatalog.model.CodeData

@Dao
interface QRDao {
    @Query("SELECT * FROM codedata")
    fun getAll(): List<CodeData>

    @Query("SELECT * FROM codedata")
    fun getAllByCompany(companyName: String): List<CodeData>

    @Insert
    fun add(dataToAdd: CodeData)

    @Delete
    fun delete(vararg dataToDelete: CodeData)

    @Update
    fun update(newData: CodeData)
}