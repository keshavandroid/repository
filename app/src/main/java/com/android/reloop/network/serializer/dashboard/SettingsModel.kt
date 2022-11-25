package com.android.reloop.network.serializer.dashboard

import com.google.gson.annotations.SerializedName

class SettingsModel {

    @SerializedName("per_day_maximum_orders_for_driver")
    var per_day_maximum_orders_for_driver: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("one_point")
    var one_point: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("reward_points_per_order_complete")
    var reward_points_per_order_complete: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("maximum_categories")
    var maximum_categories: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("delivery_fee")
    var delivery_fee: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("delivery_fee_order_limit")
    var delivery_fee_order_limit: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("collection_request_description")
    var collection_request_description: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("whatsapp_number")
    var whatsapp_Number: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("instagram_link")
    var instagram_Link: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("delivery_details")
    var delivery_Details: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("free_delivery_details")
    var free_delivery_details: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("auto_renew_subscription")
    var auto_renew_subscription: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("campaigns_visibility")
    var campaigns_visibility: ArrayList<SettingValuesModel> = null ?: ArrayList()
}