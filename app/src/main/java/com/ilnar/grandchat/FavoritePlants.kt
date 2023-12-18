package com.ilnar.grandchat

import java.io.Serializable

data class FavoritePlants (
    var image: String? = null,
    var name: String?  = null,
    var id: String,
    var input_name: String?  = null,
): Serializable