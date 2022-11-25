package com.reloop.reloop.model

class ModelReceiptOrderList(
    orderTitle: String,
    orderTitleDescription: String,

    price: String
) {

    var orderTitle: String? = ""
    var orderTitleDescription: String? = ""
    var price: String? = ""


    init {
        this.price = price
        this.orderTitleDescription = orderTitleDescription
        this.orderTitle = orderTitle

    }

}