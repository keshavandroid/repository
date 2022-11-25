package com.reloop.reloop.model

class ModelOrderHistory(
    orderTitle: String,
    orderTitleDescription: String,
    date: String,
    status: Int,
    price: String
) {

    var orderTitle: String? = ""
    var orderTitleDescription: String? = ""
    var date: String? = ""
    var status: Int?
    var price: String? = ""


    init {
        this.price = price
        this.status = status
        this.date = date
        this.orderTitleDescription = orderTitleDescription
        this.orderTitle = orderTitle

    }

}