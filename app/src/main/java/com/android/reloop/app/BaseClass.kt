package com.reloop.reloop.app

open class BaseClass {
    fun string(id: Int): String {
        return MainApplication.applicationContext().getString(id)
    }
}