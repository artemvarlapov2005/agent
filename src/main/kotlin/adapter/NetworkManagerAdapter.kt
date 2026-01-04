package org.matkini.adapter

import org.matkini.IpAddress
import org.matkini.shared.AgentData
import org.matkini.shared.ExchangeInterfaceDto
import ru.tinkoff.kora.common.Component

@Component
class DefaultNetworkManagerAdapter(
    val agentData: AgentData
) : NetworkManagerAdapter {
    override fun exchangeConfig(current : List<ExchangeInterfaceDto>?): List<ExchangeInterfaceDto> {
        return current!!
    }
}

interface NetworkManagerAdapter {
    fun exchangeConfig(current : List<ExchangeInterfaceDto>?): List<ExchangeInterfaceDto>
}