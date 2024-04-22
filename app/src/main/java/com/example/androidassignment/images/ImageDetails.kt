package com.example.androidassignment.images

import com.google.gson.annotations.SerializedName

data class ImageDetails(
    @SerializedName("id"            ) var id: String?        = null,
    @SerializedName("title"         ) var title: String? = null,
    @SerializedName("language"      ) var language: String?        = null,
    @SerializedName("thumbnail"     ) var thumbnail: Thumbnail?     = Thumbnail(),
    @SerializedName("mediaType"     ) var mediaType: Int?           = null,
    @SerializedName("coverageURL"   ) var coverageURL: String?        = null,
    @SerializedName("publishedAt"   ) var publishedAt: String?        = null,
    @SerializedName("publishedBy"   ) var publishedBy: String?        = null,
    @SerializedName("backupDetails" ) var backupDetails: BackupDetails? = BackupDetails()

)