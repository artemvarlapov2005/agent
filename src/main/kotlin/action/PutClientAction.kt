package org.matkini.action

import org.matkini.ConfigFile
import org.matkini.PeerSection
import org.matkini.controller.PutClientRequest
import org.matkini.shared.ClientResult
import org.matkini.service.InterfaceStore
import org.matkini.shared.IpAllocator
import org.matkini.shared.reloadService
import org.matkini.shared.restartService
import org.matkini.shared.getPublic
import org.matkini.shared.privateKey
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component

@Component
class PutClientAction(
    private val interfaceStore: InterfaceStore,
    private val ipAllocator: IpAllocator,
) {
    private val log = LoggerFactory.getLogger(PutClientAction::class.java)

    fun put(request: PutClientRequest) : ClientResult? {
        val config = interfaceStore.get(request.interfaceName)
            ?: error("Interface name not found")

        log.info("Текущий конфиг успешно спарсен для интерфейса ${request.interfaceName}")

        val configPublic = getPublic(config.privateKey())

        log.info("Посчитан публичный ключ сервера: $configPublic")

        config.peerSections.firstOrNull { it.publicKey == request.publicKey }?.let {
            log.info("Клиент с pubKey ${request.publicKey} уже был добавлен с айпи ${it.allowedIps.first().address}")
            return ClientResult(
                configPublic,
                it.allowedIps.first().address.toString())
        }

        val ip = ipAllocator.findFreeIp(
            (config.peerSections.flatMap { it.allowedIps } + config.interfaceSection.address).toSet(),
            config.subNet())

        log.info("Было найдено айпи $ip для клиента с pubKey ${request.publicKey} в ${request.interfaceName}")

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

        log.info("Конфиг обновлен для клиента с ${request.publicKey} в ${request.interfaceName}")

        runCatching {
            reloadService(request.interfaceName)
        }.onFailure {  e ->
            e.printStackTrace()

            interfaceStore.update(
                request.interfaceName,
                config
            )

            restartService(request.interfaceName)

            log.error("Экстренная перезагрузка для ${request.publicKey} в ${request.interfaceName}")

            error("Неверные клиентские данные")
        }.onSuccess {
            log.info("Служба перезагружена для клиента с ${request.publicKey} в ${request.interfaceName}")
        }

        return ClientResult(
            configPublic,
            ip.toString())
    }
}

fun ConfigFile.subNet() = this.interfaceSection.address.getNetwork()