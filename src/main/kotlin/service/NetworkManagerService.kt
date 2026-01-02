package org.matkini.service

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
}