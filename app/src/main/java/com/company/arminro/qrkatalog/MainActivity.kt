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
import com.company.arminro.qrkatalog.data.QRDataBase
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
import com.company.arminro.qrkatalog.logic.QRRepository
import com.company.arminro.qrkatalog.vm.MainViewModel
import com.company.arminro.qrkatalog.vm.VMFactory
import kotlinx.android.synthetic.main.data_details.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


// using the main activity in coroutine context
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope(){

    private var startMatchEnabled =  false
    private var endMatchEnabled =  false
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
        mainAdapter = MainListAdapter(this, arrayListOf()) // init with empty data
        mainList.adapter = mainAdapter

            mainVM.listData
            .nonNull()
            .observe(this) {
               mainList.invalidateViews() // updating the ui
                Log.println(Log.ASSERT, "LIST", mainVM.listData.value.toString())
            }


        startMatchEnabled = loadDataFromSharedPreferences(this, getString(R.string.settings_matches_start)) ?: false
        endMatchEnabled = loadDataFromSharedPreferences(this, getString(R.string.end_match)) ?: false
        val extras = intent.extras

        val rawData = extras?.get(getString(R.string.qr_data_intent_extra)) as? CodeData
        // if we are given a new value, we save it to the db

        if(rawData != null){
            if(rawData.id == Long.MIN_VALUE){
                mainVM.add(rawData) // smart cast!!! this will not compile without the null checking!
            }
            else{
                mainVM.update(rawData)
            }

            mainAdapter?.notifyDataSetChanged()

            Toast.makeText(MainActivity@this, "Changes saved", Toast.LENGTH_SHORT).show()
        }

        fab.setOnClickListener { view ->
            startCustomActivity(this, ScannerActivity::class)
        }

        setFilterCategories(this)
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

        val data = mainAdapter?.getItem(listPosition)

        return when (item!!.itemId) {
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
                // todo: delete
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
                startSwitch.isChecked = startMatchEnabled

                startSwitch.setOnCheckedChangeListener { _, isChecked ->
                    startMatchEnabled = isChecked

                    // save it to shared preferences for persistence, so that the options will remain after closing the app
                    saveDataToSharedPReferences(this, getString(R.string.settings_matches_start), isChecked)

                    Toast.makeText(this@MainActivity, "Start ${if (startMatchEnabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                }

                var endSwitch  = mDialogView.findViewById<Switch>(R.id.endSwitch)
                endSwitch.isChecked = endMatchEnabled
                endSwitch.setOnCheckedChangeListener { _, isChecked ->
                    endMatchEnabled = isChecked

                    saveDataToSharedPReferences(this, getString(R.string.end_match), isChecked)
                    Toast.makeText(this@MainActivity, "End ${if (endMatchEnabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
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
        mDialogView.timestamp_details.text = data?.timestampCreated
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

        val vi = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = vi.inflate(layoutId, null)

        // insert into main view after removing all existing
        val insertPoint = findViewById<ViewGroup>(anchorId)
        removeChildren(anchorId)
        insertPoint.addView(
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
                                    edit.setText("$dayOfMonth th of ${monthOfYear+1}, $year") // month of the year is 0 based!
                                    //todo: set variable here
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
                                        // todo: set variable here
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
        if(insertPoint.childCount > 0)
            insertPoint.removeAllViews()
    }






}


