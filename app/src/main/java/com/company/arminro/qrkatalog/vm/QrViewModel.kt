package com.company.arminro.qrkatalog.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.company.arminro.qrkatalog.data.QRDataBase
import com.company.arminro.qrkatalog.logic.IRepository
import com.company.arminro.qrkatalog.logic.QRRepository
import com.company.arminro.qrkatalog.model.CodeData

class QrViewModel(app: Application): AndroidViewModel(app), IQRVM {

    private val repo : IRepository
    init{
        // setting the references
        repo = QRRepository(QRDataBase.getInstance(app).qRDao())
    }

    override fun getAll() = viewModelScope.launch{

    }

    override fun getAllByCompany(companyName: String, endsWith: Boolean, beginsWith: Boolean)  {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllTo(destination: String, endsWith: Boolean, beginsWith: Boolean): LiveData<List<CodeData>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllAfter(date: String): LiveData<List<CodeData>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllBefore(date: String): LiveData<List<CodeData>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllBetween(dateStart: String, dateEnd: String): LiveData<List<CodeData>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllFrom(source: String, endsWith: Boolean, beginsWith: Boolean): LiveData<List<CodeData>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getById(id: Long): LiveData<CodeData> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(dataToAdd: CodeData) = viewModelScope.launch {
        repo.add(dataToAdd)
    }

    override fun delete(dataToDelete: CodeData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(newData: CodeData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }






}