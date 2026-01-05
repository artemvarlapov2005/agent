package org.matkini.action

import org.matkini.ConfigFile
import org.matkini.PeerSection
import org.matkini.controller.PutClientRequest
import org.matkini.shared.ClientResult
import org.matkini.service.InterfaceStore
import org.matkini.shared.IpAllocator
import org.matkini.util.getPublic
import org.matkini.util.privateKey
import ru.tinkoff.kora.common.Component

@Component
class PutClientAction(
    val interfaceStore: InterfaceStore,
    val ipAllocator: IpAllocator,
) {
    fun put(request: PutClientRequest) : ClientResult? {
        val config = interfaceStore.get(request.interfaceName)
            ?: error("Interface name to found")

        val configPublic = getPublic(config.privateKey())

        config.peerSections.firstOrNull { it.publicKey == request.publicKey }?.let {
            return ClientResult(
                configPublic,
                it.allowedIps.first().address.toString())
        }

        val ip = ipAllocator.findFreeIp(
            (config.peerSections.flatMap { it.allowedIps } + config.interfaceSection.address).toSet(),
            config.subNet())

        if (ip == null) return null

        val updatedConfig = config.copy(
            peerSections = config.peerSections + PeerSection(
                request.publicKey,
                allowedIps = listOf(ip.copy(mask = "32"))
            )
        )

        interfaceStore.update(
            request.interfaceName,
            updatedConfig)

        return ClientResult(
            configPublic,
            ip.toString())
    }
}

fun ConfigFile.subNet() = this.interfaceSection.address.getNetwork()