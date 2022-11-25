package com.android.reloop.network

import com.google.gson.annotations.SerializedName

data class ContinueAsSettings(

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class DeliveryFeeOrderLimitItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class OrganizationSignupTextItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class RewardPointsPerOrderCompleteItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class MaximumCategoriesItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class WhatsappNumberItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class DeliveryFeeItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class HouseholdSignupInfoItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class AutoRenewSubscriptionItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class PerDayMaximumOrdersForDriverItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class OnePointItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class CollectionRequestDescriptionItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class InstagramLinkItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class Data(

	@field:SerializedName("household_signup_info")
	val householdSignupInfo: List<HouseholdSignupInfoItem?>? = null,

	@field:SerializedName("collection_request_description")
	val collectionRequestDescription: List<CollectionRequestDescriptionItem?>? = null,

	@field:SerializedName("maximum_categories")
	val maximumCategories: List<MaximumCategoriesItem?>? = null,

	@field:SerializedName("one_point")
	val onePoint: List<OnePointItem?>? = null,

	@field:SerializedName("reward_points_per_order_complete")
	val rewardPointsPerOrderComplete: List<RewardPointsPerOrderCompleteItem?>? = null,

	@field:SerializedName("delivery_fee_order_limit")
	val deliveryFeeOrderLimit: List<DeliveryFeeOrderLimitItem?>? = null,

	@field:SerializedName("organization_signup_info")
	val organizationSignupInfo: List<OrganizationSignupInfoItem?>? = null,

	@field:SerializedName("delivery_fee")
	val deliveryFee: List<DeliveryFeeItem?>? = null,

	@field:SerializedName("per_day_maximum_orders_for_driver")
	val perDayMaximumOrdersForDriver: List<PerDayMaximumOrdersForDriverItem?>? = null,

	@field:SerializedName("free_delivery_details")
	val freeDeliveryDetails: List<FreeDeliveryDetailsItem?>? = null,

	@field:SerializedName("organization_signup_text")
	val organizationSignupText: List<OrganizationSignupTextItem?>? = null,

	@field:SerializedName("instagram_link")
	val instagramLink: List<InstagramLinkItem?>? = null,

	@field:SerializedName("auto_renew_subscription")
	val autoRenewSubscription: List<AutoRenewSubscriptionItem?>? = null,

	@field:SerializedName("signup_text")
	val signupText: List<SignupTextItem?>? = null,

	@field:SerializedName("delivery_details")
	val deliveryDetails: List<DeliveryDetailsItem?>? = null,

	@field:SerializedName("whatsapp_number")
	val whatsappNumber: List<WhatsappNumberItem?>? = null
)

data class SignupTextItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class FreeDeliveryDetailsItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class OrganizationSignupInfoItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class DeliveryDetailsItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("input_value")
	val inputValue: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)
