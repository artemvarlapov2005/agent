package org.matkini.action

import org.matkini.ConfigFile
import org.matkini.service.InterfaceStore
import org.matkini.shared.AgentData
import org.matkini.shared.reloadService
import org.matkini.shared.restartService
import org.matkini.toDecoded
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component

@Component
class PutConfigAction(
    private val interfaceStore: InterfaceStore,
    private val agentData: AgentData
) {
    private val log = LoggerFactory.getLogger(PutConfigAction::class.java)

    fun put(
        interfaceName: String,
        configFile: ConfigFile
    ) {
        val current = interfaceStore.get(interfaceName)

        val updatedConfigs = configFile.toDecoded(agentData.masterPassword()).copy(
            interfaceSection = configFile.interfaceSection.copy(
                additionalProperties = configFile.interfaceSection.additionalProperties?.mapValues { (_, value) ->
                    value.map { it.replace("*ipv4", interfaceName) }
                }
            )
        )

        if (updatedConfigs.compareShouldRestart(current)) {
            log.info("Полный перезапуск для интерфейса: $interfaceName")
            interfaceStore.update(interfaceName, updatedConfigs)
            restartService(interfaceName)
            log.info("Полный перезапуск для интерфейса завершен: $interfaceName")
        } else if (updatedConfigs != current) {
            log.info("Обновление для интерфейса: $interfaceName")
            interfaceStore.update(interfaceName, updatedConfigs)
            reloadService(interfaceName)
            log.info("Обновление для интерфейса завершено: $interfaceName")
        }
    }
}

private fun ConfigFile.compareShouldRestart(configFile: ConfigFile?) : Boolean {
    if (configFile == null) return true
    return this.interfaceSection.address != configFile.interfaceSection.address ||
            this.interfaceSection.dns != configFile.interfaceSection.dns ||
            this.interfaceSection.privateKey != configFile.interfaceSection.privateKey ||
            this.interfaceSection.additionalProperties != configFile.interfaceSection.additionalProperties ||
            this.interfaceSection.h1 != configFile.interfaceSection.h1 ||
            this.interfaceSection.h2 != configFile.interfaceSection.h2 ||
            this.interfaceSection.h3 != configFile.interfaceSection.h3 ||
            this.interfaceSection.h4 != configFile.interfaceSection.h4 ||
            this.interfaceSection.jc != configFile.interfaceSection.jc ||
            this.interfaceSection.jmin != configFile.interfaceSection.jmin ||
            this.interfaceSection.jmax != configFile.interfaceSection.jmax ||
            this.interfaceSection.s1 != configFile.interfaceSection.s1 ||
            this.interfaceSection.s2 != configFile.interfaceSection.s2
}