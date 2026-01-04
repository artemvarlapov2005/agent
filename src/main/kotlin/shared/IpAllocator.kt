package org.matkini.util

import org.matkini.IpAddress
import ru.tinkoff.kora.common.Component

@Component
class IpAllocator {
    fun findFreeIp(
        busyIps: Set<IpAddress>,
        subNet: IpAddress
    ) : IpAddress? {
        val used = busyIps.map { it.ipToInt() }

        subNet.let { net ->
            (net.getNetworkInt() + 1..net.getBroadcastInt() - 1).forEach {
                if (!used.contains(it)) {
                    return IpAddress.fromIpInt(it)
                }
            }
        }

        return null;
    }
}