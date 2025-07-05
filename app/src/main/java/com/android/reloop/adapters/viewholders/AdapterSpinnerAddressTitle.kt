package com.reloop.reloop.adapters.viewholders

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.serializer.Addresses

class AdapterSpinnerAddressTitle(
    textViewResourceId: Int,
    private val list: ArrayList<Addresses>?, private val icon: Drawable, var hideArrow: Boolean
) : ArrayAdapter<Addresses>(
    MainApplication.applicationContext(),
    textViewResourceId,
    list!!
) {
    override fun getCount(): Int {
        if (list != null) {
            return list.size
        }
        return 0
    }

    override fun getItem(position: Int): Addresses? {
        return list?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val label = super.getView(position, convertView, parent) as TextView
        label.setCompoundDrawablesRelativeWithIntrinsicBounds(
            icon,
            null,
            MainApplication.applicationContext()
                .getDrawable(R.drawable.icon_spinner_dropdown_arrow),
            null
        )
        if (hideArrow) {
            label.setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon,
                null,
                null,
                null
            )
        }

        if(list!!.get(position).title.isNullOrEmpty()){
            label.text = "Address Title"
        }else{
            label.text = list!!.get(position).title
        }



        return label
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun getDropDownView(
        position: Int, convertView: View?,
        parent: ViewGroup?
    ): View? {
        val inflater =
            MainApplication.applicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.spinner_dropdown_normal, null) as LinearLayout
        val label = view.getChildAt(0) as TextView
        label.text = list?.get(position)?.title

        return label
    }

}