package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.room.entity.QueryPointEntity

data class PlaceModel(
    var selected: Boolean,
    var location: QueryPointEntity
)
