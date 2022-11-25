package com.reloop.reloop.model

class ModelHomeCategories(icons: Int, points: Double, unit: String, heading: String,id:Int) {
    var icons: Int? = null
    var heading: String? = ""
    var unit: String? = ""
    var points: Double? = 0.0
    var id:Int?=0

    init {
        this.icons = icons
        this.heading = heading
        this.unit = unit
        this.points = points
        this.id=id
    }

}