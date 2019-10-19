package com.company.arminro.qrkatalog.logic

import android.arch.lifecycle.LiveData
import com.company.arminro.qrkatalog.model.CodeData

interface IRepository {
    suspend fun getAll(): LiveData<List<CodeData>>

    suspend fun getAllByCompany(companyName: String, endsWith: Boolean = false, beginsWith: Boolean = false): LiveData<List<CodeData>>

    suspend fun getAllTo(destination: String, endsWith: Boolean = false, beginsWith: Boolean = false):LiveData<List<CodeData>>
    suspend fun getAllAfter(date: String): LiveData<List<CodeData>>
    suspend fun getAllBefore(date: String): LiveData<List<CodeData>>
    suspend fun getAllBetween(dateStart: String, dateEnd: String): LiveData<List<CodeData>>
    suspend fun getAllFrom(source: String, endsWith: Boolean = false, beginsWith: Boolean = false): LiveData<List<CodeData>>
    suspend fun getById(id: Long): LiveData<CodeData>

    suspend fun add(dataToAdd: CodeData)
    suspend fun delete(dataToDelete: CodeData)
    suspend fun update(newData: CodeData)
}