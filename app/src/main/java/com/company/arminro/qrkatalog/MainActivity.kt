package com.company.arminro.qrkatalog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.util.Log
import android.view.*
import androidx.lifecycle.ViewModelProvider
import com.company.arminro.qrkatalog.model.CodeData
import com.company.arminro.qrkatalog.adapters.MainListAdapter
import com.company.arminro.qrkatalog.data.Injector
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.reflect.KClass
import com.company.arminro.qrkatalog.helpers.loadDataFromSharedPreferences
import com.company.arminro.qrkatalog.helpers.nonNull
import com.company.arminro.qrkatalog.helpers.observe
import com.company.arminro.qrkatalog.helpers.saveDataToSharedPReferences
import com.company.arminro.qrkatalog.vm.MainViewModel
import kotlinx.android.synthetic.main.data_details.view.*
import kotlinx.android.synthetic.main.double_datetime_picker.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*
import kotlinx.android.synthetic.main.search_filter.*
import kotlinx.android.synthetic.main.single_datetime_picker.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.joda.time.DateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// using the main activity in coroutine context
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope(){


    private lateinit var mainAdapter: MainListAdapter

    // normally, this would be handled by a DI container like Dagger, but to keep it simple, we use Poor Man's DI here
    private lateinit var mainVM: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // setting up dependencies
        val viewModelFactory = Injector.provideViewModelFactory(this)
        mainVM = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        // loading settings
        mainVM.setStartMatch(loadDataFromSharedPreferences(this, getString(R.string.settings_matches_start)) ?: false)
        mainVM.setEndMatch(loadDataFromSharedPreferences(this, getString(R.string.end_match)) ?: false)

        // handling data received
        val extras = intent.extras
        val rawData = extras?.getParcelable(getString(R.string.qr_data_intent_extra)) as? CodeData
        // if we are given a new value, we save it to the db

        try{
            if(rawData != null){
                if(rawData.id == 0L){
                    mainVM.add(rawData) // smart cast!!! this will not compile without the null checking!
                }
                else{
                    mainVM.update(rawData)
                }
                Toast.makeText(MainActivity@this, "Changes saved", Toast.LENGTH_SHORT).show()
            }
        }
        catch (ex: Exception){
            Toast.makeText(MainActivity@this, "Changes could not be saved", Toast.LENGTH_SHORT).show()
            Log.e("SAVE_ERROR", "Save could not be performed", ex)
        }

        mainAdapter = MainListAdapter(this, arrayListOf()) // init with empty data
        mainList.adapter = mainAdapter
        mainVM.getAll()
        mainAdapter.notifyDataSetChanged()

        mainVM.listData
            .nonNull()
            .observe(this) {
                mainAdapter.clear() // this is very slow here
                mainAdapter.addAll(it)
                mainAdapter.notifyDataSetChanged()
                //Log.println(Log.ASSERT, "LIST", mainVM.listData.value.toString())
            }


        fab.setOnClickListener {
            startCustomActivity(this, ScannerActivity::class)
        }

        setFilterCategories(this)
        filter_button.setOnClickListener { v -> run {
            filterData(filter?.selectedItem.toString())
        }}

        clear_filter_button.setOnClickListener { v -> run {
            filter.setSelection(0)
            mainVM.getAll()
        }}

        registerForContextMenu(mainList)
    }


    private fun startCustomActivity(context: Context, cls: KClass<out Activity>, data: CodeData? = null) {

        // based on: https://medium.com/@ihordzikovskyy/simplifying-passing-data-between-activities-in-android-with-kotlin-19a18125bee2
        val intent = Intent(context, cls.java)
        if(data!= null){
            intent.putExtra(getString(R.string.qr_data_intent_extra), data)
        }
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.mainlist_context, menu)
    }



    override fun onContextItemSelected(item: MenuItem?): Boolean {
    // based on: https://kotlintutorialspoint.wordpress.com/2018/03/30/context-menu-using-kotlin-in-android/
        // get the element by casting the context menu info into adapter info
        val info = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        val listPosition = info.position

        val data = mainAdapter.getItem(listPosition)

        return when (item.itemId) {
            R.id.mainListDetails ->{

                val mDialogView = buildParameterisedView(data)
                buildDialogWithView(mDialogView, data)?.show()
                return true
            }
            R.id.mainListEdit ->{
                startCustomActivity(this,ScannerActivity::class, data)
                return true
            }

            R.id.mainListDelete ->{
                mainVM.delete(data!!)
                return  true
            }
            else -> super.onOptionsItemSelected(item)
        }

        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_settings -> {

                // inflating basic settings dialog
                // the context of the dialog and toast does not matter in this case, making it the activity is just how I usually do it
                val mDialogView = LayoutInflater.from(this).inflate(R.layout.main_settings, null)
                val mBuilder = AlertDialog.Builder(this@MainActivity)
                    .setView(mDialogView)
                    .setTitle(R.string.settings_title)

                val  mAlertDialog = mBuilder.show()

                // hooking switch isEnabled to local fields
                var startSwitch  = mDialogView.findViewById<Switch>(R.id.startSwitch)
                startSwitch.isChecked = mainVM.getStartMatch()

                startSwitch.setOnCheckedChangeListener { _, isChecked ->
                    mainVM.setStartMatch(isChecked)

                    // save it to shared preferences for persistence, so that the options will remain after closing the app
                    saveDataToSharedPReferences(this, getString(R.string.settings_matches_start), isChecked)

                    Toast.makeText(this@MainActivity, "EndMatch ${if (mainVM.getStartMatch()) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                }

                var endSwitch  = mDialogView.findViewById<Switch>(R.id.endSwitch)
                endSwitch.isChecked = mainVM.getEndMatch()
                endSwitch.setOnCheckedChangeListener { _, isChecked ->
                    mainVM.setEndMatch(isChecked)

                    saveDataToSharedPReferences(this, getString(R.string.end_match), isChecked)
                    Toast.makeText(this@MainActivity, "StartMatch ${if (mainVM.getEndMatch()) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                }

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun buildDialogWithView(mDialogView: View?, data: CodeData?): AlertDialog.Builder? {
        return AlertDialog.Builder(this@MainActivity)
            .setView(mDialogView)
            .setTitle("Details")
            .setPositiveButton("Edit") { _, _ ->
                // the positive button will enable editing
                startCustomActivity(this, ScannerActivity::class, data)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
    }

    private fun buildParameterisedView(data: CodeData?): View? {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.data_details, null)
        mDialogView.timestamp_details.text = data?.timestampCreated.toString()
        mDialogView.companyField_details.text = data?.companyName
        mDialogView.toField_details.text = data?.destination
        mDialogView.fromField_details.text = data?.source
        mDialogView.description_details.text = data?.description
        return mDialogView
    }

    private fun setFilterCategories(ctx: Context){
        // based on: https://camposha.info/kotlin-android-spinner-fill-from-array-and-itemselectionlistener
        val opts = resources.getStringArray(R.array.FilterCategories)


        val filter = findViewById<Spinner>(R.id.filter)

        var adapter= ArrayAdapter(ctx,android.R.layout.simple_list_item_1,opts)
        filter.adapter=adapter
        filter.setSelection(0) // preselect the default item

        // adding views and setting appropriate properties
        // this is most probably not done right, but I got curious as to what can kotlin actually do
        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                when(i){
                    0 -> removeChildren(R.id.dynamic_anchor)
                    1, 2, 3 -> inflateAppropriateView(R.layout.search_filter, R.id.dynamic_anchor)
                    4, 5 -> {
                        inflateAppropriateView(R.layout.single_datetime_picker, R.id.dynamic_anchor)
                        addPickersToDateTime(R.id.dynamic_anchor, listOf(R.id.dateInput), listOf(R.id.timeInput))
                    }
                    6 -> {
                        inflateAppropriateView(R.layout.double_datetime_picker, R.id.dynamic_anchor)
                        addPickersToDateTime(R.id.dynamic_anchor, listOf(R.id.dateInputStart, R.id.dateInputEnd), listOf(R.id.timeInputStart, R.id.timeInputEnd))
                    }
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

    }

    private fun inflateAppropriateView(layoutId: Int, anchorId: Int){
        // dynamic view loading based on: https://stackoverflow.com/questions/6216547/android-dynamically-add-views-into-view

        removeChildren(anchorId)
        var v = layoutInflater.inflate(layoutId, null)
        //v.id = (0..10).random()



         //insert into main view after removing all existing
        removeChildren(anchorId)
        dynamic_anchor.addView(
            v,
            0,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addPickersToDateTime(parentId: Int, dates: List<Int>, times: List<Int>){

        // adding date and time pickers for every edit text on the parent
        val cal = Calendar.getInstance()

        // this is based on the following (and many others where I picked only ideas):
        // https://stackoverflow.com/questions/47170075/kotlin-ontouchlistener-called-but-it-does-not-override-performclick
        // https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
        // https://stackoverflow.com/questions/45842167/how-to-use-datepickerdialog-in-kotlin

        // this would be horrible to debug with several layers of closures and contexts
        // but for a kotlin showcase, this is neat albeit borderline unreadable
        val parent = findViewById<ViewGroup>(parentId)
        dates.forEach {
            edittextId ->
            run {

                var edit = parent.findViewById<EditText>(edittextId)
                edit.setOnTouchListener { v, event ->
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            DatePickerDialog(
                                this@MainActivity,  DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                                    // setting the text of the edit view
                                    edit.setText("$year-${monthOfYear+1}-$dayOfMonth") // month of the year is 0 based!
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                                .show()
                        }
                    }

                    v?.onTouchEvent(event) ?: true // neat: default value in case it is NULL
                }

            }
            times.forEach { edittextId ->
                run {

                    var edit = parent.findViewById<EditText>(edittextId)
                    edit.setOnTouchListener { v, event ->
                        when (event?.action) {
                            MotionEvent.ACTION_DOWN -> {
                                TimePickerDialog(this@MainActivity, TimePickerDialog
                                    .OnTimeSetListener(function = { _, h, m ->
                                        // setting the text of the edit view
                                        edit.setText("$h:${if(m < 10) "0$m" else m}") // to use 00
                                    }), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), true
                                )
                                    .show()
                            }
                        }

                        v?.onTouchEvent(event) ?: true
                    }

                }

            }
        }

    }

    private fun removeChildren(anchorId: Int){
        val insertPoint = findViewById<ViewGroup>(anchorId)
        if(insertPoint.childCount > 0){
            var onlyChild = insertPoint.getChildAt(0)
            if(onlyChild is ViewGroup)
                onlyChild.removeAllViews()
            insertPoint.removeAllViews()
        }

    }

    fun filterData(selected: String){
        // we could use the strings xml to load the values
        // but changing the file would result in different behavior (not necessarily leading to errors)


        try{
        when(selected){
            "Source" ->  mainVM.getAllFrom(findViewById<SearchView>(R.id.search_filter).query.toString())
            "Destination" -> mainVM.getAllTo(findViewById<SearchView>(R.id.search_filter).query.toString())
            "Company" -> mainVM.getAllByCompany(findViewById<SearchView>(R.id.search_filter).query.toString())
            "Earlier than" -> {
                // obviously this is only working in a very specific locale :(
                var date = findViewById<EditText>(R.id.dateInput).text.toString()
                var time = findViewById<EditText>(R.id.timeInput).text.toString().split(':')

                var newDate = DateTime(date)
                newDate.plusHours(time[0].toInt())
                newDate.plusMinutes(time[1].toInt())
                mainVM.getAllBefore(newDate.toDate())
            }
            "Later than" -> {
                var date = findViewById<EditText>(R.id.dateInput).text.toString()
                var time = findViewById<EditText>(R.id.timeInput).text.toString().split(':')

                var newDate = DateTime(date)
                newDate.plusHours(time[0].toInt())
                newDate.plusMinutes(time[1].toInt())

                mainVM.getAllAfter(newDate.toDate())
            }
            "Between" -> {
                var before = findViewById<EditText>(R.id.dateInputStart)
                var dateBefore = before.text.toString().trim()
                var timeBefore = before.text.toString().trim(':')

                var after = findViewById<EditText>(R.id.dateInputEnd)
                var dateAfter = after.text.toString().trim()
                var timeAfter = after.text.toString().trim(':')


                var newDateBefore = DateTime(dateBefore)
                newDateBefore.plusHours(timeBefore[0].toInt())
                newDateBefore.plusMinutes(timeBefore[1].toInt())

                var newDateAfter = DateTime(dateAfter)
                newDateAfter.plusHours(timeAfter[0].toInt())
                newDateAfter.plusMinutes(timeAfter[1].toInt())

                mainVM.getAllBetween(newDateBefore.toDate(), newDateAfter.toDate())
            }
            else -> mainVM.getAll()
        }
        }
        catch (ex: Exception){
            Toast.makeText(this, "Could not set the filter", Toast.LENGTH_SHORT).show()
            Log.println(Log.ERROR,"FILTER", "Filter could not be set: ${ex.message}")
        }

    }






}


