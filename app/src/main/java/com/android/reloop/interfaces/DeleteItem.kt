package com.reloop.reloop.interfaces

import com.reloop.reloop.network.serializer.Addresses

interface DeleteItem {
    fun deleteItem(position: Int)
}