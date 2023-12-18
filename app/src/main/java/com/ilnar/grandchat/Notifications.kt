package com.ilnar.grandchat

import java.io.Serializable

data class Notifications (
    var notifications_type: String? = null,
    var day_of_week: Int? = null,
    var text: String? = null,
    var id: Int? = null,
    var time: String? = null,
    var name: String? = null,
    var dockId: String,
): Serializable