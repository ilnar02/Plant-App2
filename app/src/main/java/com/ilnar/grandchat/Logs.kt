package com.ilnar.grandchat

import java.io.Serializable
import java.util.*

data class Logs (
    var record_type: String? = null,
    var date: Date? = null,
    var text: String?  = null,
    var id: String,
    var image: String? = null,
): Serializable