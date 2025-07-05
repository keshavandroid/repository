package com.reloop.reloop.adapters

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
import com.reloop.reloop.network.serializer.DependencyDetail
import com.reloop.reloop.utils.Utils
import java.lang.Exception

class AdapterSpinnerSimple(
    textViewResourceId: Int,
    private var list: ArrayList<String>?,
    private val icon: Drawable?,
    private val isSignup: Boolean
) : ArrayAdapter<String>(
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

    override fun getItem(position: Int): String {
        return list?.get(position).toString()
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
        try {
            label.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null,
                MainApplication.applicationContext().getDrawable(R.drawable.icon_spinner_dropdown_arrow), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        label.text = list?.get(position)
        if (isSignup) {

            if(position == 0){
                Utils.markRequired(label)
            }
        }
        return label
    }

    @SuppressLint("InflateParams")
    override fun getDropDownView(
        position: Int, convertView: View?,
        parent: ViewGroup?
    ): View? {
        val inflater = MainApplication.applicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.spinner_dropdown_normal, null) as LinearLayout
        val label = view.getChildAt(0) as TextView
        label.text = list?.get(position)
        return label
    }

    open fun notifyDataSetChanged(listOther: ArrayList<String>?) {
        list = listOther
        notifyDataSetChanged()
    }
}