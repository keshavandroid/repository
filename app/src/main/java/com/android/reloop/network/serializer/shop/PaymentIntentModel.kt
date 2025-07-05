package com.reloop.reloop.network.serializer

import com.google.gson.annotations.SerializedName

class PaymentIntentModel {
    @SerializedName("payment_intent_id") val payment_intend_id: String? = null
    @SerializedName("payment_intend_client_secret") val payment_intend_client_secret: String? = null

    @SerializedName("ephemeralKey") val ephemeralKey: String? = null
    @SerializedName("ephemeralKey_secret") val ephemeralKey_secret: String? = null
    @SerializedName("stripe_customer_id") val stripe_customer_id: String? = null


    @SerializedName("client_secret") val client_secret: String? = null
    @SerializedName("subscription_id") val subscription_id: String? = null
    @SerializedName("payment_intend_id") val pi_id_monthly: String? = null
    @SerializedName("stripe_subscription_id") val stripe_subscription_id: String? = null

    @SerializedName("stripe_error") val stripe_error: ArrayList<String>? = null




}