package com.company.arminro.qrkatalog

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import android.util.Log
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.data.QRDataBase
import com.company.arminro.qrkatalog.helpers.getCurrentDateTimeString
import com.company.arminro.qrkatalog.model.CodeData
import com.company.arminro.qrkatalog.helpers.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.equalTo
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.time.days

// based on: https://gabrieltanner.org/blog/android-room
@RunWith(AndroidJUnit4::class)
class DataTests {
    /*These are just very simplistic tests to check if the data layer is actually working*/
    private lateinit var dao: QRDao
    private lateinit var db: QRDataBase
    private lateinit var TIME: Date

    private fun testSetup() = runBlocking {
        // create new db and dao after every test to make sure it is isolated
        val context = InstrumentationRegistry.getContext()
        db = Room.inMemoryDatabaseBuilder(
            context, QRDataBase::class.java).build()
        dao = db.qRDao()
        TIME = getCurrentDateTime()
    }

    @Throws(IOException::class)
    private fun testTeardown() {
        db.close()
    }


    @Test
    fun addAndGetByIdTest() = runBlocking  {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", TIME )
        code.id = 0
        // act
        dao.add(code)
        val inDb: CodeData = dao.getAll()[0]
        inDb.id = 0
        // assert
        assertThat(inDb, equalTo(code))
        testTeardown()
    }

    @Test
    fun deleteTest() = runBlocking  {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", TIME )
        dao.add(code)

        // act
        dao.delete(code)
        val inDb: CodeData = dao.getById(code.id)

        // assert
        Assert.assertNull(inDb)
        testTeardown()
    }

    @Test
    fun getAllTest() = runBlocking  {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", TIME )
        val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo", TIME )
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", TIME )
        val data = setOf(code, code2, code3)

        dao.add(code)
        dao.add(code2)
        dao.add(code3)

        // act
        val result = dao.getAll()
        result.forEach { r->r.id = 0 } // the ids are autoincremented
        // assert
        Assert.assertTrue(result is List<CodeData>)

        // the the order is not guaranteed in db queries, but it is part of list equality -> sets are orderless, so only the elements matter
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllByCompanyTest() = runBlocking  {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", TIME )
        val code2: CodeData = CodeData("Obudai Egyetem", "home", "gyujtoszamla", "demo", TIME )
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", TIME )
        val data = setOf(code, code2)

        dao.add(code)
        dao.add(code2)
        dao.add(code3)

        // act
        val result = dao.getAllByCompany("%Obuda%")
        result.forEach { r->r.id = 0 } // the ids are autoincremented
        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllAfterTest() = runBlocking  {
        testSetup()
        // arrange
        val reference = DateTime.now()

        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij",  reference.minusDays(5).toDate())
        val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo", reference.toDate() )
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", reference.plusDays(5).toDate() )

        val data = setOf(code3)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)

        // act
        val result = dao.getAllAfter(reference.toDate())
        result.forEach { r->r.id = 0 } // the ids are autoincremented
        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllBeforeTest() = runBlocking  {
        testSetup()
        // arrange
        val date =  DateTime(2019, 10, 12, 5, 5, 5)

        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", date.toDate() )
        val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo", date.minusYears(1).toDate() )
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", date.plusYears(1).toDate() )

        val data = setOf(code2)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)

        // act
        val result = dao.getAllBefore(code.timestampCreated)
        result.forEach { r->r.id = 0 } // the ids are autoincremented
        // assert
        Assert.assertEquals(data, result.toSet())
        testTeardown()
    }

    @Test
    fun getAllBetweenTest() = runBlocking  {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij",DateTime(2016, 10, 10, 10, 0, 0).toDate())
        val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo", DateTime(2019, 11, 12, 10, 0, 0).toDate())
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", DateTime(2019, 11, 18, 20, 0, 0).toDate())
        val code4: CodeData = CodeData("Other_company", "home", "alamizsna", "my friend bought me dinner lol", DateTime(2020, 12, 12, 10, 0, 0).toDate())
        // proper date generation is api 26 :(
        val referenceStart = DateTime(2018, 10, 12, 10, 0, 0)
        val referenceEnd = DateTime(2020, 12, 1,1, 19, 19 )


        val data = setOf(code2, code3)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)
        dao.add(code4)

        // act
        val result = dao.getAllBetween(referenceStart.toDate(), referenceEnd.toDate())
        result.forEach { r->r.id = 0 } // the ids are autoincremented
        // assert
        Assert.assertEquals(data, result.toSet())
        testTeardown()
    }

    @Test
    fun getAllToTest() = runBlocking  {
        testSetup()
        // arrange
        val code = CodeData("Obudai Egyetem", "home", "Becsi ut 96/B", "osztondij", TIME )
        val code2 = CodeData("Obuda University", "home sweet home", "96/B Becsi street", "demo", TIME )
        val code3 = CodeData("Workplace", "Homer lol", "company_bill", "wage", TIME)
        val code4 = CodeData("Other_company", "asd", "alamizsna", "my friend bought me dinner lol", TIME )

        val data = setOf(code,code2, code3)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)
        dao.add(code4)

        // act
        val result = dao.getAllTo("%home%")
        result.forEach { r->r.id = 0 } // the ids are autoincremented
        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllFromTest()  = runBlocking {
        testSetup()
        // arrange
        val code = CodeData("Obudai Egyetem", "home", "Becsi ut 96/B", "osztondij", TIME )
        val code2 = CodeData("Obuda University", "home sweet home", "96/B Becsi street", "demo", TIME )
        val code3 = CodeData("Workplace", "Homer lol", "company_bill", "wage", TIME )
        val code4 = CodeData("Other_company", "asd", "alamizsna", "my friend bought me dinner lol",TIME )

        val data = setOf(code,code2)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)
        dao.add(code4)

        // act
        val result = dao.getAllFrom("%becs%") // upper/lowercase ignored by default :)
        result.forEach { r->r.id = 0 } // the ids are autoincremented
        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun updateTest() = runBlocking  {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", TIME )
        dao.add(code)

        val desired = CodeData("Obudai Egyetem", "otthon", "gyujtoszamla", "osztondij", TIME )
        desired.id = dao.getAll()[0].id

        // act
        dao.update(desired)
        val inDb: CodeData = dao.getAll()[0]

        // assert
        assertThat(inDb.companyName, equalTo(desired.companyName))
        assertThat(inDb.description, equalTo(desired.description))
        testTeardown()
    }


}

