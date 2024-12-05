package com.example.tpms

data class SensorData(
    val temperature: Float? = null,
    val pressure: Float? = null,
    val loaded: Float?=null,
    val unload: Float?=null,
    val tkph: Float?=null,
    val distance: Float?=null,
    val rtc: String?=null,
    val awss: Float?=null,
    val ztyrelife: String?=null,
    val latitude: Float?=null,
    val longitude: Float?=null,
    val vibration: Int?=null
)

