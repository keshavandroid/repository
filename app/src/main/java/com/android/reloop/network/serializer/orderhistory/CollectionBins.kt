package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class CollectionBins : Serializable {

    //FOR COLLECTIONS REQUEST
    @SerializedName("id")
    val id: Int? = 0

    @SerializedName("user_id")
    val user_id: Int? = 0

    @SerializedName("request_id")
    val request_id: Int? = 0

    @SerializedName("material_category_id")
    val material_category_id: Int? = 0

    @SerializedName("category_name")
    var category_name: String? = ""

    @SerializedName("weight")
    val weight: String? = ""


    @SerializedName("created_at")
    val created_at:String? = ""

    @SerializedName("updated_at")
    val updated_at: String? = ""


    @SerializedName("collection_bins")
    var collection_bins: ArrayList<CollectionBinsList>? = ArrayList()

    @SerializedName("request")
    val request: RequestBin? = RequestBin()
}

class RequestBin {
    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("user_id")
    @Expose
    private var userId: Int? = null

    @SerializedName("driver_id")
    @Expose
    private var driverId: Int? = null

    @SerializedName("supervisor_id")
    @Expose
    private var supervisorId: Any? = null

    @SerializedName("city_id")
    @Expose
    private var cityId: Int? = null

    @SerializedName("district_id")
    @Expose
    private var districtId: Int? = null

    @SerializedName("drop_off_point_id")
    @Expose
    private var dropOffPointId: Any? = null

    @SerializedName("type")
    @Expose
    private var type: String? = null

    @SerializedName("request_number")
    @Expose
    private var requestNumber: String? = null

    @SerializedName("reference_website")
    @Expose
    private var referenceWebsite: Any? = null

    @SerializedName("is_imported")
    @Expose
    private var isImported: Int? = null

    @SerializedName("confirm")
    @Expose
    private var confirm: Int? = null

    @SerializedName("collection_date")
    @Expose
    private var collectionDate: String? = null

    @SerializedName("collection_type")
    @Expose
    private var collectionType: Int? = null

    @SerializedName("reward_points")
    @Expose
    private var rewardPoints: Double? = null

    @SerializedName("status")
    @Expose
    private var status: String? = null

    @SerializedName("driver_trip_status")
    @Expose
    private var driverTripStatus: Int? = null

    @SerializedName("non_recyclable")
    @Expose
    private var nonRecyclable: Any? = null

    @SerializedName("is_subscriber")
    @Expose
    private var isSubscriber: String? = null

    @SerializedName("first_name")
    @Expose
    private var firstName: Any? = null

    @SerializedName("last_name")
    @Expose
    private var lastName: Any? = null

    @SerializedName("organization_name")
    @Expose
    private var organizationName: String? = null

    @SerializedName("phone_number")
    @Expose
    private var phoneNumber: String? = null

    @SerializedName("address_title")
    @Expose
    private var addressTitle: String? = null

    @SerializedName("location")
    @Expose
    private var location: String? = null

    @SerializedName("map_location")
    @Expose
    private var mapLocation: Any? = null

    @SerializedName("latitude")
    @Expose
    private var latitude: String? = null

    @SerializedName("longitude")
    @Expose
    private var longitude: String? = null

    @SerializedName("street")
    @Expose
    private var street: String? = null

    @SerializedName("question_1")
    @Expose
    private var question1: Any? = null

    @SerializedName("answer_1")
    @Expose
    private var answer1: Any? = null

    @SerializedName("question_2")
    @Expose
    private var question2: Any? = null

    @SerializedName("answer_2")
    @Expose
    private var answer2: Any? = null

    @SerializedName("user_comments")
    @Expose
    private var userComments: Any? = null

    @SerializedName("additional_comments")
    @Expose
    private var additionalComments: Any? = null

    @SerializedName("created_at")
    @Expose
    private var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    private var updatedAt: String? = null

    @SerializedName("user_subscription_id")
    @Expose
    private var userSubscriptionId: Int? = null

    @SerializedName("request_images")
    @Expose
    private var requestImages: List<RequestImage?>? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getUserId(): Int? {
        return userId
    }

    fun setUserId(userId: Int?) {
        this.userId = userId
    }

    fun getDriverId(): Int? {
        return driverId
    }

    fun setDriverId(driverId: Int?) {
        this.driverId = driverId
    }

    fun getSupervisorId(): Any? {
        return supervisorId
    }

    fun setSupervisorId(supervisorId: Any?) {
        this.supervisorId = supervisorId
    }

    fun getCityId(): Int? {
        return cityId
    }

    fun setCityId(cityId: Int?) {
        this.cityId = cityId
    }

    fun getDistrictId(): Int? {
        return districtId
    }

    fun setDistrictId(districtId: Int?) {
        this.districtId = districtId
    }

    fun getDropOffPointId(): Any? {
        return dropOffPointId
    }

    fun setDropOffPointId(dropOffPointId: Any?) {
        this.dropOffPointId = dropOffPointId
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }

    fun getRequestNumber(): String? {
        return requestNumber
    }

    fun setRequestNumber(requestNumber: String?) {
        this.requestNumber = requestNumber
    }

    fun getReferenceWebsite(): Any? {
        return referenceWebsite
    }

    fun setReferenceWebsite(referenceWebsite: Any?) {
        this.referenceWebsite = referenceWebsite
    }

    fun getIsImported(): Int? {
        return isImported
    }

    fun setIsImported(isImported: Int?) {
        this.isImported = isImported
    }

    fun getConfirm(): Int? {
        return confirm
    }

    fun setConfirm(confirm: Int?) {
        this.confirm = confirm
    }

    fun getCollectionDate(): String? {
        return collectionDate
    }

    fun setCollectionDate(collectionDate: String?) {
        this.collectionDate = collectionDate
    }

    fun getCollectionType(): Int? {
        return collectionType
    }

    fun setCollectionType(collectionType: Int?) {
        this.collectionType = collectionType
    }

    fun getRewardPoints(): Double? {
        return rewardPoints
    }

    fun setRewardPoints(rewardPoints: Double?) {
        this.rewardPoints = rewardPoints
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getDriverTripStatus(): Int? {
        return driverTripStatus
    }

    fun setDriverTripStatus(driverTripStatus: Int?) {
        this.driverTripStatus = driverTripStatus
    }

    fun getNonRecyclable(): Any? {
        return nonRecyclable
    }

    fun setNonRecyclable(nonRecyclable: Any?) {
        this.nonRecyclable = nonRecyclable
    }

    fun getIsSubscriber(): String? {
        return isSubscriber
    }

    fun setIsSubscriber(isSubscriber: String?) {
        this.isSubscriber = isSubscriber
    }

    fun getFirstName(): Any? {
        return firstName
    }

    fun setFirstName(firstName: Any?) {
        this.firstName = firstName
    }

    fun getLastName(): Any? {
        return lastName
    }

    fun setLastName(lastName: Any?) {
        this.lastName = lastName
    }

    fun getOrganizationName(): String? {
        return organizationName
    }

    fun setOrganizationName(organizationName: String?) {
        this.organizationName = organizationName
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    fun setPhoneNumber(phoneNumber: String?) {
        this.phoneNumber = phoneNumber
    }

    fun getAddressTitle(): String? {
        return addressTitle
    }

    fun setAddressTitle(addressTitle: String?) {
        this.addressTitle = addressTitle
    }

    fun getLocation(): String? {
        return location
    }

    fun setLocation(location: String?) {
        this.location = location
    }

    fun getMapLocation(): Any? {
        return mapLocation
    }

    fun setMapLocation(mapLocation: Any?) {
        this.mapLocation = mapLocation
    }

    fun getLatitude(): String? {
        return latitude
    }

    fun setLatitude(latitude: String?) {
        this.latitude = latitude
    }

    fun getLongitude(): String? {
        return longitude
    }

    fun setLongitude(longitude: String?) {
        this.longitude = longitude
    }

    fun getStreet(): String? {
        return street
    }

    fun setStreet(street: String?) {
        this.street = street
    }

    fun getQuestion1(): Any? {
        return question1
    }

    fun setQuestion1(question1: Any?) {
        this.question1 = question1
    }

    fun getAnswer1(): Any? {
        return answer1
    }

    fun setAnswer1(answer1: Any?) {
        this.answer1 = answer1
    }

    fun getQuestion2(): Any? {
        return question2
    }

    fun setQuestion2(question2: Any?) {
        this.question2 = question2
    }

    fun getAnswer2(): Any? {
        return answer2
    }

    fun setAnswer2(answer2: Any?) {
        this.answer2 = answer2
    }

    fun getUserComments(): Any? {
        return userComments
    }

    fun setUserComments(userComments: Any?) {
        this.userComments = userComments
    }

    fun getAdditionalComments(): Any? {
        return additionalComments
    }

    fun setAdditionalComments(additionalComments: Any?) {
        this.additionalComments = additionalComments
    }

    fun getCreatedAt(): String? {
        return createdAt
    }

    fun setCreatedAt(createdAt: String?) {
        this.createdAt = createdAt
    }

    fun getUpdatedAt(): String? {
        return updatedAt
    }

    fun setUpdatedAt(updatedAt: String?) {
        this.updatedAt = updatedAt
    }

    fun getUserSubscriptionId(): Int? {
        return userSubscriptionId
    }

    fun setUserSubscriptionId(userSubscriptionId: Int?) {
        this.userSubscriptionId = userSubscriptionId
    }

    fun getRequestImages(): List<RequestImage?>? {
        return requestImages
    }

    fun setRequestImages(requestImages: List<RequestImage?>?) {
        this.requestImages = requestImages
    }
}

class RequestImage {
    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("request_id")
    @Expose
    private var requestId: Int? = null

    @SerializedName("user_id")
    @Expose
    private var userId: Int? = null

    @SerializedName("collection_bin_id")
    @Expose
    private var collectionBinId: Any? = null

    @SerializedName("image")
    @Expose
    private var image: String? = null

    @SerializedName("created_at")
    @Expose
    private var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    private var updatedAt: String? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getRequestId(): Int? {
        return requestId
    }

    fun setRequestId(requestId: Int?) {
        this.requestId = requestId
    }

    fun getUserId(): Int? {
        return userId
    }

    fun setUserId(userId: Int?) {
        this.userId = userId
    }

    fun getCollectionBinId(): Any? {
        return collectionBinId
    }

    fun setCollectionBinId(collectionBinId: Any?) {
        this.collectionBinId = collectionBinId
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

    fun getCreatedAt(): String? {
        return createdAt
    }

    fun setCreatedAt(createdAt: String?) {
        this.createdAt = createdAt
    }

    fun getUpdatedAt(): String? {
        return updatedAt
    }

    fun setUpdatedAt(updatedAt: String?) {
        this.updatedAt = updatedAt
    }
}
