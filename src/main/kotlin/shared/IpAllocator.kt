package org.matkini.shared

import org.matkini.IpAddress
import ru.tinkoff.kora.common.Component

@Component
class IpAllocator {
    fun findFreeIp(
        busyIps: Set<IpAddress>,
        subNet: IpAddress
    ) : IpAddress? {
        val used = busyIps.map { it.ipToInt() }

        return IpAddress.fromIpInt(
            (subNet.getNetworkInt() + 1..subNet.getBroadcastInt() - 1)
                .firstOrNull {
                    !used.contains(it)
                } ?: return null)
    }
}