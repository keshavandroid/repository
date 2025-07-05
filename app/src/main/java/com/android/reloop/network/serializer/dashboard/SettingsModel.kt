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

    @SerializedName("dropoff_visibility")
    var dropoff_visibility: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("google_pay_visibility")
    var googlepay_visibility: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("coffee_capsules_household_visibility")
    var coffee_capsules_household_visibility: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("soap_bars_household_visibility")
    var soap_bars_household_visibility: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("reports_download_button_for_household")
    var reports_download_button_for_household: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("reports_download_button_for_organization")
    var reports_download_button_for_organization: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("show_recycling_family_names")
    var show_recycling_family_names: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("photo_taking_for_collection_request")
    var photo_taking_for_collection_request: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("show_text_on_bottom_of_home_screen")
    var show_text_on_bottom_of_home_screen: ArrayList<SettingValuesModel> = null ?: ArrayList()


    @SerializedName("driver_photo_visibility_to_households")
    var driver_photo_visibility_to_households: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("driver_photo_visibility_to_organizations")
    var driver_photo_visibility_to_organizations: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("supervisor_photo_visibility_to_households")
    var supervisor_photo_visibility_to_households: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("supervisor_photo_visibility_to_organizations")
    var supervisor_photo_visibility_to_organizations: ArrayList<SettingValuesModel> = null ?: ArrayList()


    @SerializedName("households_collection_photo_visibility")
    var households_collection_photo_visibility: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("organizations_collection_photo_visibility")
    var organizations_collection_photo_visibility: ArrayList<SettingValuesModel> = null ?: ArrayList()

    @SerializedName("collection_time")
    var collection_time: ArrayList<SettingValuesModel> = null ?: ArrayList()
}