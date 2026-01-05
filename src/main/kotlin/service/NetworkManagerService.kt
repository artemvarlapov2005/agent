package org.matkini.service

import org.matkini.adapter.NetworkManagerAdapter
import org.matkini.shared.ExchangeInterfaceDto
import ru.tinkoff.kora.common.Component

@Component
class NetworkManagerService(
    private val networkManagerAdapter: NetworkManagerAdapter
) {
    fun exchangeConfig(current: List<ExchangeInterfaceDto>?) : List<ExchangeInterfaceDto> = runCatching {
        networkManagerAdapter.exchangeConfig(current)
    }.getOrElse { e ->
        if (current == null) {
            throw e
        }
        return current
    }
}