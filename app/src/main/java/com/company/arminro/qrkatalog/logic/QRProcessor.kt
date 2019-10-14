package com.company.arminro.qrkatalog.logic

import android.arch.persistence.room.Room
import android.content.Context
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.data.QRDataBase
import com.company.arminro.qrkatalog.model.CodeData

class QRProcessor {
    private var repo: QRDao? = null
    constructor(repo: QRDao){
        this.repo =  repo
    }


    fun getAll(): List<CodeData>?{
        return repo?.getAll()
    }


    // this could have a more clever solution, also very ugly code duplication, but didnt have much time :(
    fun getAllByCompany(companyName: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>?{

        /*Because how sql works it is easy to let the user decide what kind of equality s/he wants:
        * - total word equality (both are false)
        * - all results ending with the desired word
        * - all results beginning with the desired word
        * - all results containing the word anywhere (both are true)
        * */
        return if(endsWith && beginsWith)
            repo?.getAllByCompany("%$companyName%")
        else if(endsWith && !beginsWith)
            repo?.getAllByCompany("%$companyName")
        else if(!endsWith && beginsWith)
            repo?.getAllByCompany("$companyName%")
        else
            repo?.getAllByCompany(companyName)
    }


    fun getAllTo(destination: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>?{
        return if(endsWith && beginsWith)
            repo?.getAllTo("%$destination%")
        else if(endsWith && !beginsWith)
            repo?.getAllTo("%$destination")
        else if(!endsWith && beginsWith)
            repo?.getAllTo("$destination%")
        else
            repo?.getAllTo(destination)
    }

    fun getAllAfter(date: String): List<CodeData>?{
        return repo?.getAllAfter(date)
    }

    fun getAllBefore(date: String): List<CodeData>? {
        return repo?.getAllBefore(date)
    }

    fun getAllBetween(dateStart: String, dateEnd: String): List<CodeData>?{
        return repo?.getAllBetween(dateStart, dateEnd)
    }

    fun getAllFrom(source: String, endsWith: Boolean = false, beginsWith: Boolean = false): List<CodeData>? {
        return if(endsWith && beginsWith)
            repo?.getAllTo("%$source%")
        else if(endsWith && !beginsWith)
            repo?.getAllTo("%$source")
        else if(!endsWith && beginsWith)
            repo?.getAllTo("$source%")
        else
            repo?.getAllTo(source)
    }

    fun getById(id: String): CodeData? {
        return repo?.getById(id)
    }

    fun add(dataToAdd: CodeData){
        repo?.add(dataToAdd)
    }

    fun delete(dataToDelete: CodeData){
       repo?.delete(dataToDelete)
    }

    fun update(newData: CodeData){
        repo?.update(newData)
    }
}