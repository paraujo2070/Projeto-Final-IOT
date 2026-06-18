package com.example.app_proprietario.data

object SampleData {
    val properties = listOf(
        Property(
            id = "1",
            name = "Casa de Praia",
            rooms = listOf(
                Room("1a", "Sala",     humidity = 50, temperature = 22,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("1b", "Cozinha",  humidity = 43, temperature = 23,
                    intrusionStatus = IntrusionStatus.INTRUSION_DETECTED, moldStatus = MoldStatus.NO_RISK),
                Room("1c", "Banheiro", humidity = 76, temperature = 31,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.RISK_DETECTED)
            )
        ),
        Property(
            id = "2",
            name = "Apartamento Bloco 2 Apto 206",
            rooms = listOf(
                Room("2a", "Sala",    humidity = 55, temperature = 24,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("2b", "Quarto",  humidity = 60, temperature = 23,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("2c", "Cozinha", humidity = 48, temperature = 22,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("2d", "Banheiro", humidity = 70, temperature = 28,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK)
            )
        ),
        Property(
            id = "3",
            name = "Mansão",
            rooms = listOf(
                Room("3a", "Sala",       humidity = 52, temperature = 22,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("3b", "Cozinha",    humidity = 45, temperature = 23,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("3c", "Banheiro 1", humidity = 68, temperature = 29,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("3d", "Banheiro 2", humidity = 72, temperature = 30,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.RISK_DETECTED),
                Room("3e", "Quarto 1",   humidity = 55, temperature = 22,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("3f", "Quarto 2",   humidity = 53, temperature = 22,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("3g", "Escritório", humidity = 50, temperature = 21,
                    intrusionStatus = IntrusionStatus.NO_INTRUSION, moldStatus = MoldStatus.NO_RISK),
                Room("3h", "Garagem",    humidity = 60, temperature = 25,
                    intrusionStatus = IntrusionStatus.INTRUSION_DETECTED, moldStatus = MoldStatus.NO_RISK)
            )
        )
    )
}