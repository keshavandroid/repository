package com.reloop.reloop.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.reloop.reloop.fragments.BaseFragment
import java.util.*


class ViewPagerAdapter(fm: FragmentManager?, fragments: ArrayList<BaseFragment>) : FragmentPagerAdapter(fm!!) {
    var list: ArrayList<BaseFragment> = fragments
    override fun getItem(position: Int): Fragment {
        return list[position]

    }

    override fun getCount(): Int {
        return list.size
    }


}