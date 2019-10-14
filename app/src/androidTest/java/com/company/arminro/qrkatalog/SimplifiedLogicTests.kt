package com.company.arminro.qrkatalog

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.company.arminro.qrkatalog.data.QRDao
import com.company.arminro.qrkatalog.data.QRDataBase
import com.company.arminro.qrkatalog.logic.QRProcessor
import com.company.arminro.qrkatalog.model.CodeData
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.annotation.RetentionPolicy
import java.util.stream.Collectors.toSet


// normally we would want to test the whole logic
// but normally logic is doing much more than data transfer and some formatting
@RunWith(AndroidJUnit4::class)
class SimplifiedLogicTests{

    private lateinit var logic: QRProcessor
    private lateinit var db: QRDataBase

    // initializing the test with some data
    private val code: CodeData = CodeData("Obuda University", "ANYTHING home", "gyujtoszamla", "osztondij" )
    private val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo" )
    private val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage" )
    private val code4: CodeData = CodeData("Other_company", "home ANYTHING", "alamizsna", "my friend bought me dinner lol" )
    private val code5: CodeData = CodeData("Other_company", "ANYTHING home ANYTHING", "alamizsna", "my friend bought me dinner again" )

    @Before
    fun testSetup(){
        val context = InstrumentationRegistry.getContext()
        db = Room.inMemoryDatabaseBuilder(context, QRDataBase::class.java).build()
        var repo  = db.QRDao()

        repo.add(code)
        repo.add(code2)
        repo.add(code3)
        repo.add(code4)
        repo.add(code5)

        logic = QRProcessor(repo) // poor man's dependency injection will do just fine here :)
    }

    @After
    @Throws(IOException::class)
    fun testTeardown() {
        db.close()
    }

    @Test
    fun stringTotalEqualityTest() {

        // arrange
        val desired = setOf(code2,  code3)

        // act
        val result = logic.getAllTo("home")

        // assert
        Assert.assertEquals(desired, result?.toSet())
    }

    @Test
    fun stringEqualityAndEndingWithDesiredWord() {

        // arrange
        val desired = setOf(code, code2, code3)

        // act
        val result = logic.getAllTo("%home")

        // assert
        Assert.assertEquals(desired, result?.toSet())
    }

    @Test
    fun stringEqualityAndBeginningWithDesiredWord() {

        // arrange
        val desired = setOf(code2, code3, code4)

        // act
        val result = logic.getAllTo("home%")

        // assert
        Assert.assertEquals(desired, result?.toSet())
    }

    @Test
    fun stringEqualityAndBeginningOrEndingWithDesiredWord() {

        // arrange
        val desired = setOf(code, code2, code3, code4, code5)

        // act
        val result = logic.getAllTo("%home%")

        // assert
        Assert.assertEquals(desired, result?.toSet())
    }

}