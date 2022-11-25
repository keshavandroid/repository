package com.android.reloop.network.serializer.Campain

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class ProductDetail {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("product_name")
    @Expose
    private var productName: String? = null

    @SerializedName("status")
    @Expose
    private var status: Int? = null

    @SerializedName("barcode")
    @Expose
    private var barcode: String? = null

    @SerializedName("recyclable_percentage")
    @Expose
    private var recyclablePercentage: Int? = null

    @SerializedName("brand_logo")
    @Expose
    private var brandLogo: String? = null

    @SerializedName("description")
    @Expose
    private var description: String? = null

    @SerializedName("product_images")
    @Expose
    private var productImages: List<ProductImage?>? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getProductName(): String? {
        return productName
    }

    fun setProductName(productName: String?) {
        this.productName = productName
    }

    fun getStatus(): Int? {
        return status
    }

    fun setStatus(status: Int?) {
        this.status = status
    }

    fun getBarcode(): String? {
        return barcode
    }

    fun setBarcode(barcode: String?) {
        this.barcode = barcode
    }

    fun getRecyclablePercentage(): Int? {
        return recyclablePercentage
    }

    fun setRecyclablePercentage(recyclablePercentage: Int?) {
        this.recyclablePercentage = recyclablePercentage
    }

    fun getBrandLogo(): String? {
        return brandLogo
    }

    fun setBrandLogo(brandLogo: String?) {
        this.brandLogo = brandLogo
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    fun getProductImages(): List<ProductImage?>? {
        return productImages
    }

    fun setProductImages(productImages: List<ProductImage?>?) {
        this.productImages = productImages
    }

    class ProductImage {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("barcode_id")
        @Expose
        var barcodeId: Int? = null

        @SerializedName("image")
        @Expose
        var image: String? = null
    }

}