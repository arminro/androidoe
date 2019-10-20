package com.company.arminro.qrkatalog.data

import android.content.Context
import com.company.arminro.qrkatalog.logic.IRepository
import com.company.arminro.qrkatalog.logic.QRRepository
import com.company.arminro.qrkatalog.vm.VMFactory

// based on: https://github.com/AhsenSaeed/RoomPersistenceCoroutines

// singleton class to have only 1 provider
object Injector {

    // this could be a fluent builer fabricating different view models with different dao and repo layers
    private fun provideDataAccess(context: Context): IRepository {
        return QRRepository(QRDataBase.getInstance(context).qRDao())
    }

    fun provideViewModelFactory(context: Context): VMFactory {
        val repo = provideDataAccess(context)
        return VMFactory(repo)
    }
}