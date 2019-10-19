package com.company.arminro.qrkatalog.logic

import android.arch.lifecycle.LiveData
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.model.CodeData


class QRRepository(private val repo: QRDao) : IRepository {


    override suspend fun getAll()  : LiveData<List<CodeData>> {
        return repo.getAll()
    }

    // this could have a more clever solution, also very ugly code duplication, but didnt have much time :(
    override suspend fun getAllByCompany(companyName: String, endsWith: Boolean, beginsWith: Boolean) : LiveData<List<CodeData>> {

        /*Because how sql works it is easy to let the user decide what kind of equality s/he wants:
        * - total word equality (both are false)
        * - all results ending with the desired word
        * - all results beginning with the desired word
        * - all results containing the word anywhere (both are true)
        * */
        if(endsWith && beginsWith)
            repo.getAllByCompany("%$companyName%")
        else if(endsWith && !beginsWith)
            repo.getAllByCompany("%$companyName")
        else if(!endsWith && beginsWith)
            repo.getAllByCompany("$companyName%")
        else
            repo.getAllByCompany(companyName)
    }

    override suspend fun getAllTo(destination: String, endsWith: Boolean, beginsWith: Boolean) : LiveData<List<CodeData>>{
        return if(endsWith && beginsWith)
            repo.getAllTo("%$destination%")
        else if(endsWith && !beginsWith)
            repo.getAllTo("%$destination")
        else if(!endsWith && beginsWith)
            repo.getAllTo("$destination%")
        else
            repo.getAllTo(destination)
    }

    override suspend fun getAllAfter(date: String) : LiveData<List<CodeData>> {
        return repo.getAllAfter(date)
    }

    override suspend fun getAllBefore(date: String) : LiveData<List<CodeData>> {
        return repo.getAllBefore(date)
    }

    override suspend fun getAllBetween(dateStart: String, dateEnd: String) : LiveData<List<CodeData>> {
        return repo.getAllBetween(dateStart, dateEnd)
    }

    override suspend fun getAllFrom(source: String, endsWith: Boolean, beginsWith: Boolean) : LiveData<List<CodeData>> {
        return if(endsWith && beginsWith)
            repo.getAllTo("%$source%")
        else if(endsWith && !beginsWith)
            repo.getAllTo("%$source")
        else if(!endsWith && beginsWith)
            repo.getAllTo("$source%")
        else
            repo.getAllTo(source)
    }

    override suspend fun getById(id: Long) : LiveData<CodeData> {
        return repo.getById(id)
    }

    override suspend fun add(dataToAdd: CodeData) {
        repo.add(dataToAdd)
    }

    override suspend fun delete(dataToDelete: CodeData) {
        repo.delete(dataToDelete)
    }

    override suspend fun update(newData: CodeData) {
        repo.update(newData)
    }
}