package com.ilnar.grandchat

import java.io.Serializable

data class Plant (
    var image: String? = null,
    var ruName: String?  = null,
    var description: String? = null,
    var ltName: String?  = null,
    var fact: String? = null,
    var lifeExp: String? = null,
    var floweringTime: String? = null,
    var plantHeight: String? = null,
    var flowerColor: String? = null,
    var leafColor: String? = null,
    var watering: String? = null,
    var topDressing: String? = null,
    var pruning: String? = null,
    var soil: String? = null,
    var light: String? = null,
    var temperature: String? = null,
    var phylum: String? = null,
    var clas: String? = null,
    var order: String? = null,
    var family: String? = null,
    var genus: String? = null,
    var species: String? = null,
    var disease: String? = null,
    var id: Int? = null,
): Serializable
