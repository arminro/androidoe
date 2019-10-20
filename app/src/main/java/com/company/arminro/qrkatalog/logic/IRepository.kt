package com.company.arminro.qrkatalog.logic

import androidx.lifecycle.LiveData
import com.company.arminro.qrkatalog.model.CodeData

interface IRepository {
    suspend fun getAll(): List<CodeData>

    suspend fun getAllByCompany(companyName: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>

    suspend fun getAllTo(destination: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>
    suspend fun getAllAfter(date: String): List<CodeData>
    suspend fun getAllBefore(date: String): List<CodeData>
    suspend fun getAllBetween(dateStart: String, dateEnd: String): List<CodeData>
    suspend fun getAllFrom(source: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>
    suspend fun getById(id: Long): CodeData

    suspend fun add(dataToAdd: CodeData)
    suspend fun delete(dataToDelete: CodeData)
    suspend fun update(newData: CodeData)
}