package com.company.arminro.qrkatalog.vm

import android.arch.lifecycle.LiveData
import com.company.arminro.qrkatalog.model.CodeData

interface IQRVM {
    fun getAll(): LiveData<List<CodeData>>
    fun getAllByCompany(companyName: String, endsWith: Boolean = false, beginsWith: Boolean = false): LiveData<List<CodeData>>
    fun getAllTo(destination: String, endsWith: Boolean = false, beginsWith: Boolean = false): LiveData<List<CodeData>>
    fun getAllAfter(date: String): LiveData<List<CodeData>>
    fun getAllBefore(date: String): LiveData<List<CodeData>>
    fun getAllBetween(dateStart: String, dateEnd: String): LiveData<List<CodeData>>
    fun getAllFrom(source: String, endsWith: Boolean = false, beginsWith: Boolean = false): LiveData<List<CodeData>>
    fun getById(id: Long): LiveData<CodeData>
    fun add(dataToAdd: CodeData)
    fun delete(dataToDelete: CodeData)
    fun update(newData: CodeData)
}