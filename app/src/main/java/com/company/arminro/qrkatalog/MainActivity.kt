package com.company.arminro.qrkatalog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.view.ViewGroup
import android.widget.TextView
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.MotionEvent
import android.widget.DatePicker
import kotlinx.android.synthetic.main.double_datetime_picker.*
import java.time.Month
import java.time.MonthDay
import java.util.*
import javax.xml.datatype.DatatypeConstants.MONTHS


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var starMatchEnabled = false
    private var endMatchEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        setFilterCategories(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
                startSwitch.setOnCheckedChangeListener { _, isChecked ->
                    starMatchEnabled = isChecked
                    Toast.makeText(this@MainActivity, "Start ${if (starMatchEnabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                }

                var endSwitch  = mDialogView.findViewById<Switch>(R.id.endSwitch)
                endSwitch.setOnCheckedChangeListener { _, isChecked ->
                    endMatchEnabled = isChecked
                    Toast.makeText(this@MainActivity, "End ${if (endMatchEnabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                }

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
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

