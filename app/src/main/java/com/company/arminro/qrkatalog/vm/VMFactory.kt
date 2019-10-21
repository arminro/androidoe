package com.company.arminro.qrkatalog.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.logic.IRepository
import com.company.arminro.qrkatalog.logic.QRRepository

// based on: https://github.com/AhsenSaeed/RoomPersistenceCoroutines
class VMFactory(private val repo: IRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(repo) as T
        else
        throw IllegalArgumentException("Unknown View Model class")
    }
}
