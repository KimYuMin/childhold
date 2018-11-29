package com.example.yuminkim.childhold.model

data class Child constructor(val idx: Int,
                             val name: String,
                             val beaconId: String,
                             val latLng: LatLng,
                             val deviceId: String,
                             val parentId: String) {
    override fun equals(other: Any?) = if (other is Child) {
        if (other.idx == idx &&
                other.name == name &&
                other.beaconId == beaconId &&
                other.deviceId == deviceId &&
                other.parentId == parentId) {
            true
        } else {
            false
        }
    } else false

}