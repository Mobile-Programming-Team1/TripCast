package com.example.tripcast.model

object DestinationDataFactory {

    fun makeDestinationList(): List<Destination> {
        return listOf(
            Destination("New York", "Sunny", "26°C", "High"),
            Destination("Los Angeles", "Rainy", "22°C", "Low"),
            Destination("Miami", "Cloudy", "24°C", "Medium"),
            Destination("San Francisco", "Sunny", "28°C", "High")
        )
    }
}