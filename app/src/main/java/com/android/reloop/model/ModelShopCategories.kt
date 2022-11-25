package com.reloop.reloop.model

class ModelShopCategories(icons:Int, heading:String) {

    constructor(icons:Int, heading:String, selectedCheck: Boolean?) : this(icons, heading) {
        this.selected=selectedCheck
    }
    constructor(icons:Int, heading:String, type: Int?) : this(icons, heading) {
        this.type=type
    }

    var type:Int?=0
    var icons: Int? = null
    var heading: String? = ""
    var selected:Boolean?=false

    init {
        this.icons = icons
        this.heading = heading
    }
}
