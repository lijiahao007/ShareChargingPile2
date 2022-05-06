package com.lijiahao.sharechargingpile2.data

data class AppointmentInfo(
    val station: ChargingPileStation,
    val pile: ChargingPile?,
    val appointment: Appointment
)
