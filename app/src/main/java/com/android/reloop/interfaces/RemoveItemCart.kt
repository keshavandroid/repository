package com.reloop.reloop.interfaces

import com.reloop.reloop.network.serializer.shop.Category

interface RemoveItemCart{
    fun removeItem(item: Category?)
}