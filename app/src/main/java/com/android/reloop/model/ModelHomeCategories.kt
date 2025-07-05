package com.reloop.reloop.model

class ModelHomeCategories(status: String, icons: Int, points: Double, unit: String, heading: String,id:Int,headingReport:String) {
    var status: String? = ""
    var icons: Int? = null
    var heading: String? = ""
    var unit: String? = ""
    var points: Double? = 0.0
    var id:Int?=0
    var headingReport:String?=""

    init {
        this.status = status
        this.icons = icons
        this.heading = heading
        this.unit = unit
        this.points = points
        this.id=id
        this.headingReport=headingReport
    }

}