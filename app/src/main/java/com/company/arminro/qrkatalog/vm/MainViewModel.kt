package com.company.arminro.qrkatalog.vm

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.company.arminro.qrkatalog.R
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.data.QRDataBase
import com.company.arminro.qrkatalog.helpers.NonNullMediatorLiveData
import com.company.arminro.qrkatalog.helpers.getCurrentDateTimeString
import com.company.arminro.qrkatalog.logic.IRepository
import com.company.arminro.qrkatalog.logic.QRRepository
import com.company.arminro.qrkatalog.model.CodeData
import kotlinx.coroutines.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MainViewModel(private val repo: IRepository): ViewModelBase() {

    private val listDataMediator = NonNullMediatorLiveData<List<CodeData>>()
    val listData = listDataMediator
    private val currentItemMediator = NonNullMediatorLiveData<CodeData>()
    val cuurentItem = currentItemMediator

    private var startMatchEnabled =  false
    private var endMatchEnabled =  false
    private var selectedFilterCategory = ""


    fun getStartMatch() : Boolean {
        return startMatchEnabled
    }

    fun setStartMatch(current: Boolean)  {
        startMatchEnabled = current
    }

    fun getEndMatch() : Boolean {
        return endMatchEnabled
    }

    fun setEndMatch(current: Boolean)  {
        endMatchEnabled = current
    }


    fun setSelectedFilterCategory(selected: String)  {
        selectedFilterCategory = selected
    }


    fun getAllTo(destination: String){
        if(destination.isBlank())
            throw IllegalArgumentException("The destination must have a value")
        getAllTo(destination, endMatchEnabled, startMatchEnabled)
    }

    fun getAllByCompany(companyName: String){
        if(companyName.isBlank())
            throw IllegalArgumentException("The company name must have a value")
        getAllByCompany(companyName, endMatchEnabled, startMatchEnabled)
    }

    fun getAll() = launch(coroutineContext) {
        var data = repo.getAll()
        Log.println(Log.ASSERT, "GetALL_RESULT", data.toString())
        listDataMediator.postValue(data)
    }

    fun getAllFrom(source: String){
        if(source.isBlank())
            throw IllegalArgumentException("The property source must have a value")
        getAllFrom(source, endMatchEnabled, startMatchEnabled)
    }


    fun getAllAfter(date: String)  = launch(coroutineContext) {
        if(date.isBlank())
            throw IllegalArgumentException("The property date must have a value")
        listDataMediator.postValue(repo.getAllAfter(date))
    }

    fun getAllBefore(date: String) = launch(coroutineContext) {
        if(date.isBlank())
            throw IllegalArgumentException("The property date must have a value")
        listDataMediator.postValue(repo.getAllBefore(date))
    }

    fun getAllBetween(dateStart: String, dateEnd: String) = launch(coroutineContext) {
        if(dateStart.isBlank() || dateEnd.isBlank())
            throw IllegalArgumentException("Both properties must have a value")
        listDataMediator.postValue(repo.getAllBetween(dateStart, dateEnd))
    }

    fun getById(id: Long) = launch(coroutineContext) {
        currentItemMediator.postValue(repo.getById(id))
    }

    fun add(dataToAdd: CodeData) = launch(coroutineContext){
        repo.add(dataToAdd)
        Log.println(Log.ASSERT, "ADD", dataToAdd.toString())
        getAll() // updating the ui with all the elements
    }

    fun delete(dataToDelete: CodeData) = launch(coroutineContext) {
        repo.delete(dataToDelete)

        getAll() // updating the ui will all the elements
    }

    fun update(newData: CodeData) = launch(coroutineContext) {
        repo.update(newData)
        getAll()
    }


    // the view just sets the values where it needs to
    private fun getAllByCompany(companyName: String, endsWith: Boolean, beginsWith: Boolean) = launch(coroutineContext)  {
        listDataMediator.postValue(repo.getAllByCompany(companyName, endsWith, beginsWith))
    }

    private fun getAllTo(destination: String, endsWith: Boolean, beginsWith: Boolean) = launch(coroutineContext)  {
        listDataMediator.postValue(repo.getAllTo(destination, endsWith, beginsWith))
    }


    private fun getAllFrom(source: String, endsWith: Boolean, beginsWith: Boolean) = launch(coroutineContext) {
        listDataMediator.postValue(repo.getAllFrom(source, endsWith, beginsWith))
    }








}