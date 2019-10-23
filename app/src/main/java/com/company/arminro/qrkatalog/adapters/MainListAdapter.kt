package com.company.arminro.qrkatalog.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.company.arminro.qrkatalog.R
import com.company.arminro.qrkatalog.model.CodeData
import kotlinx.android.synthetic.main.data_list_element.view.*
import android.R.attr.data
import android.text.method.TextKeyListener.clear
import androidx.recyclerview.widget.DiffUtil





class MainListAdapter(context: Context, private var data: List<CodeData>) :
    ArrayAdapter<CodeData>(context, R.layout.data_list_element, data) {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): CodeData? {
        return data[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(
                R.layout.data_list_element, null
            ) as View
        }

        // setting the appropriate element texts
        val codeDataElement = data[position]
        convertView.companyField.text = codeDataElement.companyName
        convertView.timestamp.text = codeDataElement.timestampCreated.toString()
        convertView.fromField.text = codeDataElement.source
        convertView.toField.text = codeDataElement.destination

        // enabling long click
        convertView.isLongClickable = true
        return convertView
    }




}