package org.matkini.service

import org.matkini.IpAddress
import ru.tinkoff.kora.common.Component

@Component
class IpAllocator {
    fun findFreeIp(
        busyIps: Set<IpAddress>,
        allowedSubNets: List<IpAddress>
    ) : IpAddress? {
        val used = busyIps.map { it.ipToInt() }

        allowedSubNets.forEach {
            (it.getNetworkInt() + 1..it.getBroadcastInt() - 1).forEach {
                if (!used.contains(it)) {
                    return IpAddress.fromIpInt(it)
                }
            }
        }

        return null;
    }
}