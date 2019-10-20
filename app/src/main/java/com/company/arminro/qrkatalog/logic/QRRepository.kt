package com.company.arminro.qrkatalog.logic

import androidx.lifecycle.LiveData
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.model.CodeData


class QRRepository(private val qrDao: QRDao) : IRepository {
    override suspend fun getAll(): List<CodeData> {
        return qrDao.getAll()
    }


    override suspend fun getAllByCompany(companyName: String, endsWith: Boolean, beginsWith: Boolean) : List<CodeData> {

        /*Because how sql works it is easy to let the user decide what kind of equality s/he wants:
        * - total word equality (both are false)
        * - all results ending with the desired word
        * - all results beginning with the desired word
        * - all results containing the word anywhere (both are true)
        * */
        return if(endsWith && beginsWith)
            qrDao.getAllByCompany("%$companyName%")
        else if(endsWith && !beginsWith)
            qrDao.getAllByCompany("%$companyName")
        else if(!endsWith && beginsWith)
            qrDao.getAllByCompany("$companyName%")
        else
            qrDao.getAllByCompany(companyName)
    }

    override suspend fun getAllTo(destination: String, endsWith: Boolean, beginsWith: Boolean) :List<CodeData> {
        return if(endsWith && beginsWith)
            qrDao.getAllTo("%$destination%")
        else if(endsWith && !beginsWith)
            qrDao.getAllTo("%$destination")
        else if(!endsWith && beginsWith)
            qrDao.getAllTo("$destination%")
        else
            qrDao.getAllTo(destination)
    }

    override suspend fun getAllAfter(date: String) : List<CodeData> {
        return qrDao.getAllAfter(date)
    }

    override suspend fun getAllBefore(date: String) : List<CodeData> {
        return qrDao.getAllBefore(date)
    }

    override suspend fun getAllBetween(dateStart: String, dateEnd: String) : List<CodeData> {
        return qrDao.getAllBetween(dateStart, dateEnd)
    }

    override suspend fun getAllFrom(source: String, endsWith: Boolean, beginsWith: Boolean) : List<CodeData> {
        return if(endsWith && beginsWith)
            qrDao.getAllTo("%$source%")
        else if(endsWith && !beginsWith)
            qrDao.getAllTo("%$source")
        else if(!endsWith && beginsWith)
            qrDao.getAllTo("$source%")
        else
            qrDao.getAllTo(source)
    }

    override suspend fun getById(id: Long) : CodeData {
        return qrDao.getById(id)
    }

    override suspend fun add(dataToAdd: CodeData) {
        qrDao.add(dataToAdd)
    }

    override suspend fun delete(dataToDelete: CodeData) {
        qrDao.delete(dataToDelete)
    }

    override suspend fun update(newData: CodeData) {
        qrDao.update(newData)
    }
}