package com.reloop.reloop.enums

object OrderHistoryEnum {

    //OLD "status" FOR SUBSCRIPTION
    /*val ACTIVE = 1;
    val PENDING = 2;
    val COMPLETED_SUB = 3;
    val EXPIRED = 4

    //new added
    val SUB_CANCELLED = 5
    val SUB_REFUNDED = 6
    val SUB_REFUND_REQUEST = 7*/

    //NEW "status" FOR SUBSCRIPTION

    val ACTIVE = "Active";
    val PENDING = "Valid";
    val COMPLETED_SUB = "Used";
    val EXPIRED = "Expired"

    //new added
    val SUB_CANCELLED = "Cancelled"
    val SUB_REFUNDED = "Refunded"
    val SUB_REFUND_REQUEST = "Renewed"

//=====================================================================================================

    //OLD "status" FOR ORDER
    /*val NOT_ASSIGNED = 1
    val ASSIGNED = 2
    val TRIP_INITIATED = 3
    val COMPLETED = 4
    val CANCELLED = 5
    val ORDER_REFUNDED = 6
    val REFUND_REQUEST = 7
    val ORDER_VERIFIED = 8*/


    //NEW "status" FOR ORDER
    val NOT_ASSIGNED = "New"
    val ASSIGNED = "Assigned"
    val TRIP_INITIATED = "Dispatched"
    val COMPLETED = "Completed"
    val CANCELLED = "Cancelled"
    val ORDER_REFUNDED = "Refunded"
    val REFUND_REQUEST = "Refund-Request"
    val ORDER_VERIFIED = "Verified"
}