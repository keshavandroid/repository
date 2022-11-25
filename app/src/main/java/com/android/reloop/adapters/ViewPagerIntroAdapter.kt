package com.reloop.reloop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.reloop.reloop.app.MainApplication

class ViewPagerIntroAdapter(private var layouts: IntArray) : PagerAdapter() {
    private var layoutInflater: LayoutInflater? = null

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater =
            MainApplication.applicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view: View? = layoutInflater?.inflate(layouts[position], container, false)
        container.addView(view)
        return view!!
    }

    override fun getCount(): Int {
        return this.layouts.size
    }

    override fun isViewFromObject(
        view: View,
        obj: Any
    ): Boolean {
        return view === obj
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        val view = `object` as View
        container.removeView(view)
    }
}