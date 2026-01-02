package org.matkini.adapter

import org.matkini.IpAddress
import ru.tinkoff.kora.common.Component

@Component
class DefaultNetworkManagerAdapter : NetworkManagerAdapter {
    override fun getAllowedSubNets(): List<IpAddress> {
        return listOf(IpAddress("10.0.0.0", "24"))
    }
}

