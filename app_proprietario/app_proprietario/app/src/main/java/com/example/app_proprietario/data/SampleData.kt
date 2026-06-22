package com.example.app_proprietario.data

object SampleData {
    val mockedProperty = Property(
        id = "1",
        name = "Casa de Praia (Mock)",
        rooms = listOf(
            Room("1a", "Sala",     humidity = 50, temperature = 22,
                intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
            Room("1b", "Cozinha",  humidity = 43, temperature = 23,
                intrusionStatus = IntrusionStatus.INTRUSION_DETECTED, moldStatus = MoldStatus.NO_RISK),
            Room("1c", "Banheiro", humidity = 76, temperature = 31,
                intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.RISK_DETECTED),
            Room("1d", "Quarto",   humidity = 55, temperature = 24,
                intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK)
        )
    )
}