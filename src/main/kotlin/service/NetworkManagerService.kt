package org.matkini.service

import org.matkini.ConfigFile
import org.matkini.IpAddress
import org.matkini.adapter.NetworkManagerAdapter
import ru.tinkoff.kora.common.Component
@Component
class NetworkManagerService(
    val networkManagerAdapter: NetworkManagerAdapter
) {
    fun getAllowedSubNets() : List<IpAddress> = runCatching {
        networkManagerAdapter.getAllowedSubNets()
    }.getOrElse {
        emptyList()
    }

    fun exchangeConfig(current: ConfigFile?) : ConfigFile = runCatching {
        networkManagerAdapter.getCurrentConfig(current)
    }.getOrElse { e ->
        if (current == null) {
            throw e
        }
        return current
    }
}