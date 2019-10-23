package com.company.arminro.qrkatalog.logic

import androidx.lifecycle.LiveData
import com.company.arminro.qrkatalog.model.CodeData
import java.util.*

interface IRepository {
    suspend fun getAll(): List<CodeData>

    suspend fun getAllByCompany(companyName: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>

    suspend fun getAllTo(destination: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>
    suspend fun getAllAfter(date: Date): List<CodeData>
    suspend fun getAllBefore(date: Date): List<CodeData>
    suspend fun getAllBetween(dateStart: Date, dateEnd: Date): List<CodeData>
    suspend fun getAllFrom(source: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>
    suspend fun getById(id: Long): CodeData

    suspend fun add(dataToAdd: CodeData)
    suspend fun delete(dataToDelete: CodeData)
    suspend fun update(newData: CodeData)
}