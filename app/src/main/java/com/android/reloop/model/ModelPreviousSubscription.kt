package com.reloop.reloop.model

class ModelPreviousSubscription(
    subscriptionTitle: String,
    subscriptionStatus: String,
    subscriptionAmount: String,
    subscriptionRemainingDays: String,
    subscriptionDate: String,
    subscriptionIcon: Int
) {

    var subscriptionTitle: String? = ""
    var subscriptionStatus: String? = ""
    var subscriptionAmount: String? = ""
    var subscriptionRemainingDays: String? = ""
    var subscriptionDate: String? = ""
    var subscriptionIcon: Int? = -1


    init {
        this.subscriptionTitle = subscriptionTitle
        this.subscriptionStatus = subscriptionStatus
        this.subscriptionAmount = subscriptionAmount
        this.subscriptionRemainingDays = subscriptionRemainingDays
        this.subscriptionDate = subscriptionDate
        this.subscriptionIcon = subscriptionIcon

    }

}