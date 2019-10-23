package com.company.arminro.qrkatalog

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.company.arminro.qrkatalog.data.QRDataBase
import com.company.arminro.qrkatalog.helpers.getCurrentDateTime
import com.company.arminro.qrkatalog.helpers.getCurrentDateTimeString
import com.company.arminro.qrkatalog.logic.QRRepository
import com.company.arminro.qrkatalog.model.CodeData
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


// normally we would want to test the whole logic
// but normally logic is doing much more than data transfer and some formatting
@RunWith(AndroidJUnit4::class)
class SimplifiedLogicTests{

    private lateinit var logic: QRRepository
    private lateinit var db: QRDataBase
    private var TEST_TIME  = getCurrentDateTime()

    // initializing the test with some data
    private val code: CodeData = CodeData("Obuda University", "ANYTHING home", "gyujtoszamla", "osztondij",TEST_TIME )
    private val code2: CodeData = CodeData("Obuda University", "home", "gyujtoszamla", "demo",TEST_TIME )
    private val code3: CodeData = CodeData("Workplace", "home", "company_bill", "wage",TEST_TIME )
    private val code4: CodeData = CodeData("Other_company", "home ANYTHING", "alamizsna", "my friend bought me dinner lol",TEST_TIME )
    private val code5: CodeData = CodeData("Other_company", "ANYTHING home ANYTHING", "alamizsna", "my friend bought me dinner again",TEST_TIME )

    @Before
    fun testSetup() = runBlocking {
        val context = InstrumentationRegistry.getContext()
        db = Room.inMemoryDatabaseBuilder(context, QRDataBase::class.java).build()
        var repo  = db.qRDao()

        repo.add(code)
        repo.add(code2)
        repo.add(code3)
        repo.add(code4)
        repo.add(code5)

        logic = QRRepository(repo) // poor man's dependency injection will do just fine here :)
    }

    @After
    @Throws(IOException::class)
    fun testTeardown() {
        db.close()
    }

    @Test
    fun stringTotalEqualityTest() = runBlocking {

        // arrange
        val desired = setOf(code2,  code3)

        // act
        val result = logic.getAllTo("home")
        result.forEach { r->r.id = 0 }
        // assert
        Assert.assertEquals(desired, result?.toSet())
    }

    @Test
    fun stringEqualityAndEndingWithDesiredWord() = runBlocking {

        // arrange
        val desired = setOf(code, code2, code3)

        // act
        val result = logic.getAllTo("%home")
        result.forEach { r->r.id = 0 }
        // assert
        Assert.assertEquals(desired, result?.toSet())
    }

    @Test
    fun stringEqualityAndBeginningWithDesiredWord() = runBlocking {

        // arrange
        val desired = setOf(code2, code3, code4)

        // act
        val result = logic.getAllTo("home%")
        result.forEach { r->r.id = 0 }

        // assert
        Assert.assertEquals(desired, result?.toSet())
    }


    @Test
    fun stringEqualityAndBeginningOrEndingWithDesiredWord() = runBlocking  {

        // arrange
        val desired = setOf(code, code2, code3, code4, code5)

        // act
        val result = logic.getAllTo("%home%")
        result.forEach { r->r.id = 0 }
        // assert
        Assert.assertEquals(desired, result?.toSet())
    }

}