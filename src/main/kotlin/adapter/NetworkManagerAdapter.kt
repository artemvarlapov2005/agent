package org.matkini.adapter

import org.matkini.ConfigFile
import org.matkini.InterfaceSection
import org.matkini.IpAddress
import org.matkini.shared.AgentData
import org.matkini.shared.ExchangeInterfaceDto
import ru.tinkoff.kora.common.Component

@Component
class DefaultNetworkManagerAdapter(
    val agentData: AgentData
) : NetworkManagerAdapter {
    override fun exchangeConfig(current : List<ExchangeInterfaceDto>?): List<ExchangeInterfaceDto> {
        return listOf(
            ExchangeInterfaceDto("wg0",
                ConfigFile(
                    interfaceSection = InterfaceSection(
                        address = IpAddress.fromString("10.0.0.1/24"),
                        privateKey = "qMnLqC02VX+v0phNtocxkKZ9Bbf/ncPoYqCODNjGbH4=",
                        jc = 6,
                        jmin = 8,
                        jmax = 80,
                        s1 = 21,
                        s2 = 126,
                        h1 = 664839111,
                        h2 = 1485638111,
                        h3 = 1457145111,
                        h4 = 1956374111,
                        ),
                    peerSections = listOf()
                ))
        )
    }
}

interface NetworkManagerAdapter {
    fun exchangeConfig(current : List<ExchangeInterfaceDto>?): List<ExchangeInterfaceDto>
}