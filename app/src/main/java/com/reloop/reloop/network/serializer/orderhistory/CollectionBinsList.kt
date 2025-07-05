package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class CollectionBinsList : Serializable{
    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("request_collection_id")
    @Expose
    private var requestCollectionId: Int? = null

    @SerializedName("location_bin_id")
    @Expose
    private var locationBinId: Int? = null

    @SerializedName("status")
    @Expose
    private var status: String? = null

    @SerializedName("weight")
    @Expose
    private var weight: String? = null

    @SerializedName("reason")
    @Expose
    private var reason: Any? = null

    @SerializedName("image")
    @Expose
    private var image: Any? = null

    @SerializedName("created_at")
    @Expose
    private var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    private var updatedAt: String? = null

    @SerializedName("deleted_at")
    @Expose
    private var deletedAt: Any? = null

    @SerializedName("request_images")
    @Expose
    private var requestImages: ArrayList<RequestImage?>? = null

    @SerializedName("location_bin")
    @Expose
    private var locationBin: LocationBin? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getRequestCollectionId(): Int? {
        return requestCollectionId
    }

    fun setRequestCollectionId(requestCollectionId: Int?) {
        this.requestCollectionId = requestCollectionId
    }

    fun getLocationBinId(): Int? {
        return locationBinId
    }

    fun setLocationBinId(locationBinId: Int?) {
        this.locationBinId = locationBinId
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getWeight(): String? {
        return weight
    }

    fun setWeight(weight: String?) {
        this.weight = weight
    }

    fun getReason(): Any? {
        return reason
    }

    fun setReason(reason: Any?) {
        this.reason = reason
    }

    fun getImage(): Any? {
        return image
    }

    fun setImage(image: Any?) {
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

    fun getDeletedAt(): Any? {
        return deletedAt
    }

    fun setDeletedAt(deletedAt: Any?) {
        this.deletedAt = deletedAt
    }

    fun getRequestImages(): ArrayList<RequestImage?>? {
        return requestImages
    }

    fun setRequestImages(requestImages: ArrayList<RequestImage?>?) {
        this.requestImages = requestImages
    }

    fun getLocationBin(): LocationBin? {
        return locationBin
    }

    fun setLocationBin(locationBin: LocationBin?) {
        this.locationBin = locationBin
    }


    class LocationBin {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("address_location_id")
        @Expose
        var addressLocationId: Int? = null

        @SerializedName("material_category_id")
        @Expose
        var materialCategoryId: Int? = null

        @SerializedName("qr_title")
        @Expose
        var qrTitle: String? = null

        @SerializedName("image")
        @Expose
        var image: String? = null

        @SerializedName("created_at")
        @Expose
        var createdAt: String? = null

        @SerializedName("updated_at")
        @Expose
        var updatedAt: String? = null

        @SerializedName("deleted_at")
        @Expose
        var deletedAt: Any? = null

        @SerializedName("material_category")
        @Expose
        var materialCategory: MaterialCategory? = null

        @SerializedName("address_location")
        @Expose
        var addressLocation: AddressLocation? = null
    }


    class MaterialCategory {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("recycling_family_id")
        @Expose
        var recyclingFamilyId: Int? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("collection_acceptance_days")
        @Expose
        var collectionAcceptanceDays: List<String>? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("avatar")
        @Expose
        var avatar: String? = null

        @SerializedName("status")
        @Expose
        var status: Int? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Int? = null

        @SerializedName("unit")
        @Expose
        var unit: Int? = null

        @SerializedName("reward_points")
        @Expose
        var rewardPoints: Int? = null

        @SerializedName("visible_in_dropoff")
        @Expose
        var visibleInDropoff: Int? = null

        @SerializedName("allowed_cities")
        @Expose
        var allowedCities: String? = null

        @SerializedName("co2_emission_reduced")
        @Expose
        var co2EmissionReduced: Double? = null

        @SerializedName("trees_saved")
        @Expose
        var treesSaved: Double? = null

        @SerializedName("oil_saved")
        @Expose
        var oilSaved: Double? = null

        @SerializedName("electricity_saved")
        @Expose
        var electricitySaved: Double? = null

        @SerializedName("water_saved")
        @Expose
        var waterSaved: Int? = null

        @SerializedName("landfill_space_saved")
        @Expose
        var landfillSpaceSaved: Double? = null

        @SerializedName("compost_created")
        @Expose
        var compostCreated: Double? = null

        @SerializedName("cigarette_butts_saved")
        @Expose
        var cigaretteButtsSaved: Int? = null

        @SerializedName("biodiesel_produced")
        @Expose
        var biodieselProduced: Int? = null

        @SerializedName("farming_land")
        @Expose
        var farmingLand: Double? = null

        @SerializedName("coffee_capsule")
        @Expose
        var coffeeCapsule: Int? = null

        @SerializedName("soap_bars")
        @Expose
        var soapBars: Int? = null

        @SerializedName("created_at")
        @Expose
        var createdAt: String? = null

        @SerializedName("updated_at")
        @Expose
        var updatedAt: String? = null

        @SerializedName("deleted_at")
        @Expose
        var deletedAt: String? = null

        @SerializedName("product_for")
        @Expose
        var productFor: Int? = null
    }


    class AddressLocation {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("address_id")
        @Expose
        var addressId: Int? = null

        @SerializedName("location")
        @Expose
        var location: String? = null

        @SerializedName("created_at")
        @Expose
        var createdAt: String? = null

        @SerializedName("updated_at")
        @Expose
        var updatedAt: String? = null

        @SerializedName("deleted_at")
        @Expose
        var deletedAt: Any? = null
    }

}
