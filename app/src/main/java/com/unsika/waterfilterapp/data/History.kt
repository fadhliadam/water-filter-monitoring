package com.unsika.waterfilterapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class History(
    val date: String = "",
    val temp_value: Double = 0.0,
    val turbi_status: String = "",
    val turbi_value: Double = 0.0
)
