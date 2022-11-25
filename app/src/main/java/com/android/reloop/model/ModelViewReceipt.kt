package com.reloop.reloop.model

import com.reloop.reloop.network.serializer.orderhistory.OrderItems

class ModelViewReceipt(
    orderTitle: String?,
    orderDateTime: String?,
    address: String?,
    status: Int?,
    orderID: String?,
    orderList: ArrayList<OrderItems>?,
    trackList: ArrayList<ModelTrackOrder>?,orderId:Int
) {

    var orderTitle: String? = ""
    var orderDateTime: String? = ""
    var address: String? = ""
    var status: Int?
    var orderID: String? = ""
    var orderList: ArrayList<OrderItems>?
    var trackList:ArrayList<ModelTrackOrder>?
    var orderId:Int =0

    init {
        this.orderTitle = orderTitle
        this.orderDateTime = orderDateTime
        this.address = address
        this.status = status
        this.orderID = orderID
        this.orderList=orderList
        this.trackList=trackList
        this.orderId=orderId

    }

}