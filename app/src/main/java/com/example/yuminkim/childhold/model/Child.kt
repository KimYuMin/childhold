package com.example.yuminkim.childhold.model

data class Child constructor(val idx: Int,
                             val name: String,
                             val beaconId: String,
                             val latLng: LatLng,
                             val deviceId: String)