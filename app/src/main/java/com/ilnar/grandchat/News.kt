package com.ilnar.grandchat

import java.util.*

data class News (
    val text: String? = null,
    val author: String? = null,
    val date: Date? = null,
    val image: String? = null,
    var likes: Long? = 0, // значение по умолчанию - 0
    val dockId: String
)