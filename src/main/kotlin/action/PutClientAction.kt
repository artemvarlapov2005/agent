package org.matkini.action

import org.matkini.PeerSection
import org.matkini.adapter.NetworkManagerAdapter
import org.matkini.dto.ClientResult
import org.matkini.service.ConfigFileService
import org.matkini.service.IpAllocator
import org.matkini.service.NetworkManagerService
import org.matkini.service.PublicKeyProvider
import ru.tinkoff.kora.common.Component

@Component
class PutClientAction(
    val configFileService: ConfigFileService,
    val ipAllocator: IpAllocator,
    val networkManagerService: NetworkManagerService,
    val publicKeyProvider: PublicKeyProvider
) {
    fun put(pubKey : String) : ClientResult? {
        val config = configFileService.get()

        config.peerSections.firstOrNull { it.publicKey == pubKey }?.let {
            return ClientResult(
                publicKeyProvider.provide(),
                it.allowedIps.first().address.toString())
        }

        val ip = ipAllocator.findFreeIp(
            (config.peerSections.flatMap { it.allowedIps } + config.interfaceSection.address).toSet(),
            networkManagerService.getAllowedSubNets())

        if (ip == null) return null

        val updatedConfig = config.copy(
            peerSections = config.peerSections + PeerSection(
                pubKey,
                allowedIps = listOf(ip.copy(mask = "32"))
            )
        )

        configFileService.update(updatedConfig)

        return ClientResult(
            publicKeyProvider.provide(),
            ip.toString())
    }
}