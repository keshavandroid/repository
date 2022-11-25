package com.reloop.reloop.eventbus

class MessageEvent(value: Int) {
    var value: Int? = null

    init {
        this.value = value
    }
}