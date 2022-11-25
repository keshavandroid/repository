package com.reloop.reloop.model

class ModelTrackOrder(check:Boolean, heading:String) {

    var check: Boolean? = null
    var heading: String? = ""

    init {
        this.check = check
        this.heading = heading
    }
}
