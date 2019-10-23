package com.company.arminro.qrkatalog.adapters

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import com.company.arminro.qrkatalog.R
import com.company.arminro.qrkatalog.model.CodeData
import kotlinx.android.synthetic.main.data_list_element.view.*


// based om: https://android.jlelse.eu/using-recyclerview-in-android-kotlin-722991e86bf3

class CodeDataAdapter(private val items : List<CodeData>, val context: Context)
    : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.data_list_element,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(parent: ViewHolder, position: Int) {

        // acquiring an element and filling the appropriate textview with value
        val codeDataElement = items[position]
        parent.company?.text = codeDataElement.companyName
        parent.timeStamp?.text = codeDataElement.timestampCreated.toString()
        parent.from?.text = codeDataElement.source
        parent.to?.text = codeDataElement.destination

        // setting the long click for the holder of the view elements
        parent.itemView.setOnLongClickListener {
            setPosition(parent.adapterPosition)
            false
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private var position: Int = 0

    fun getPosition(): Int {
        return position
    }

    private fun setPosition(position: Int) {
        this.position = position
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

        v?.setOnCreateContextMenuListener(this)

        // based on: https://kotlintutorialspoint.wordpress.com/2018/03/30/context-menu-using-kotlin-in-android/
        menu?.setHeaderTitle(v?.findViewById<TextView>(R.id.timestamp)?.text)

        menu?.add(Menu.NONE, v!!.id, 0, "Edit")
        menu?.add(Menu.NONE, v!!.id, 1, "Details")
        menu?.add(Menu.NONE, v!!.id, 2, "Delete")


    }


    val company: TextView? = view.data_list_holder.companyField
    val timeStamp: TextView?  = view.data_list_holder.timestamp
    val from: TextView?  = view.data_list_holder.fromField
    val to: TextView?  = view.data_list_holder.toField
}