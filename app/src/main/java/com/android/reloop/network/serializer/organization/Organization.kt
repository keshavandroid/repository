package com.reloop.reloop.network.serializer.organization

import com.google.gson.annotations.SerializedName

class Organization {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("org_external_id")
    var org_external_id: String? = ""
    @SerializedName("name")
    var name: String? = ""
    @SerializedName("no_of_employees")
    var no_of_employees: String? = ""
    @SerializedName("no_of_branches")
    var no_of_branches: String? = ""
    @SerializedName("sector_id")
    var sector_id: Int? = 0
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""
}