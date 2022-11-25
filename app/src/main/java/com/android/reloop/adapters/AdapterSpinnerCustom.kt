package com.reloop.reloop.adapters

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
import com.reloop.reloop.network.serializer.DependencyDetail
import com.reloop.reloop.utils.Utils


open class AdapterSpinnerCustom(
    textViewResourceId: Int,
     var list: ArrayList<DependencyDetail>?,
     val icon: Drawable,
    private val isSignup:Boolean
) : ArrayAdapter<DependencyDetail>(
    MainApplication.applicationContext(),
    textViewResourceId,
    list!!
) {
    override fun getCount(): Int {
        if (list != null) {
            return list!!.size
        }
        return 0
    }

    override fun getItem(position: Int): DependencyDetail? {
        return list?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val label = super.getView(position, convertView, parent) as TextView
        label.setCompoundDrawablesRelativeWithIntrinsicBounds(
            icon,
            null,
            MainApplication.applicationContext().getDrawable(R.drawable.icon_spinner_dropdown_arrow),
            null
        )
        label.text = list?.get(position)?.name.toString()
        if (isSignup) {
            Utils.markRequired(label)
        }
        return label
    }

    override fun getDropDownView(
        position: Int, convertView: View?,
        parent: ViewGroup?
    ): View? {
        val inflater =
            MainApplication.applicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.spinner_dropdown_normal, null) as LinearLayout
        val label = view.getChildAt(0) as TextView
        label.text = list?.get(position)?.name
        return label
    }

    open fun notifyDataSetChanged(list: ArrayList<DependencyDetail>?)
    {
        this.list=list
        notifyDataSetChanged()
    }
}