package com.company.arminro.qrkatalog

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.data.QRDataBase
import com.company.arminro.qrkatalog.helpers.getCurrentDateTimeString
import com.company.arminro.qrkatalog.model.CodeData
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// based on: https://gabrieltanner.org/blog/android-room
@RunWith(AndroidJUnit4::class)
class DataTests {
    /*These are just very simplistic tests to check if the data layer is actually working*/
    private lateinit var dao: QRDao
    private lateinit var db: QRDataBase
    private lateinit var TIME: String

    private fun testSetup(){
        // create new db and dao after every test to make sure it is isolated
        val context = InstrumentationRegistry.getContext()
        db = Room.inMemoryDatabaseBuilder(
            context, QRDataBase::class.java).build()
        dao = db.qRDao()
        TIME = getCurrentDateTimeString()
    }

    @Throws(IOException::class)
    private fun testTeardown() {
        db.close()
    }


    @Test
    fun addAndGetByIdTest() {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", TIME )
        code.id = 0
        // act
        dao.add(code)
        val inDb: CodeData = dao.getAll()[0]

        // assert
        assertThat(inDb, equalTo(code))
        testTeardown()
    }

    @Test
    fun deleteTest() {
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
    fun getAllTest() {
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

        // assert
        Assert.assertTrue(result is List<CodeData>)

        // the the order is not guaranteed in db queries, but it is part of list equality -> sets are orderless, so only the elements matter
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllByCompanyTest() {
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

        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllAfterTest() {
        testSetup()
        // arrange
        val reference = "20191012_100000"
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", "20191201_122100" )
        val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo", reference )
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", "20191221_122100" )

        val data = setOf(code, code3)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)

        // act
        val result = dao.getAllAfter(reference)

        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllBeforeTest() {
        testSetup()
        // arrange
        val reference = "20191012_100000"
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", "20181201_122100" )
        val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo", reference )
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", "20181221_122100" )

        val data = setOf(code, code3)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)

        // act
        val result = dao.getAllBefore(reference)

        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllBetweenTest() {
        testSetup()
        // arrange
        val code: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "osztondij", "20161010_100000" )
        val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo", "20191112_100000" )
        val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage", "20191118_200000" )
        val code4: CodeData = CodeData("Other_company", "home", "alamizsna", "my friend bought me dinner lol", "20201112_100000" )
        // proper date generation is api 26 :(
        val referenceStart = "20191012_100000"
        val referenceEnd = "20191201_191919"


        val data = setOf(code2, code3)
        dao.add(code)
        dao.add(code2)
        dao.add(code3)
        dao.add(code4)

        // act
        val result = dao.getAllBetween(referenceStart, referenceEnd)

        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllToTest() {
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

        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun getAllFromTest() {
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

        // assert
        Assert.assertEquals(result.toSet(), data)
        testTeardown()
    }

    @Test
    fun updateTest() {
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

