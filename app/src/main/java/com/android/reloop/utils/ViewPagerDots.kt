package com.android.reloop.utils

import android.content.Context
import android.text.Html
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.reloop.reloop.R


object ViewPagerDots {

    lateinit var dots: Array<TextView?>
    fun addBottomDots(i: Int, requireContext: Context, layoutDots: LinearLayout, listsize: Int) {
        dots = arrayOfNulls(listsize)

        //Log.e("TAG","====list size on dots===" + listsize)
        val colorsActive = requireContext.resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = requireContext.resources.getIntArray(R.array.array_dot_inactive)
        layoutDots.removeAllViews()
        for (i in (dots.indices)) {
            dots[i] = TextView(requireContext)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 40f
           // dots[i]?.setTextColor(colorsInactive[i])
            layoutDots.addView(dots[i])
        }
        if (dots.isNotEmpty()) {
            dots[i]?.setTextColor(requireContext.resources.getColor(R.color.green_color_button))
        }
    }
}