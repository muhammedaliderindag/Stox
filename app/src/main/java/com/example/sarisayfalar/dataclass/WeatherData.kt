package com.example.sarisayfalar.dataclass

data class WeatherData(
    val date: String,
    val day: String,
    val icon: String,
    val description: String,
    val status: String,
    val degree: String,
    val min: String,
    val max: String,
    val night: String,
    val humidity: String
)