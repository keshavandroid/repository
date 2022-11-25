package com.reloop.reloop.fragments

import androidx.fragment.app.Fragment

open class  BaseFragment : Fragment() {

    protected var fragmentName = ""

    companion object
    {
        private var cartOpened=false

        fun isCartCycleOpened():Boolean{
            return cartOpened
        }
        fun setCartOpened(value:Boolean)
        {
            this.cartOpened=value
        }
    }
}
