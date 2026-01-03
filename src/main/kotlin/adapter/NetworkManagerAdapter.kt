package org.matkini.adapter

import org.matkini.ConfigFile
import org.matkini.IpAddress
import org.matkini.service.AgentData
import ru.tinkoff.kora.common.Component

@Component
class DefaultNetworkManagerAdapter(
    val agentData: AgentData
) : NetworkManagerAdapter {
    override fun getAllowedSubNets(): List<IpAddress> {
        return listOf(IpAddress("10.0.0.0", "24"))
    }

    override fun getCurrentConfig(current : ConfigFile?): ConfigFile {
        return current!!
    }
}

interface NetworkManagerAdapter {
    fun getAllowedSubNets() : List<IpAddress>

    fun getCurrentConfig(current: ConfigFile?) : ConfigFile
}