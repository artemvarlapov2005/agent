package org.matkini.service

import org.matkini.ConfigFile
import org.matkini.shared.AgentData
import org.matkini.shared.enableService
import org.matkini.shared.getDefaultIPv4Interface
import org.matkini.shared.installPackage
import org.matkini.shared.isPackageInstalled
import org.matkini.shared.reloadService
import org.matkini.shared.restartService
import org.matkini.toDecoded
import ru.tinkoff.kora.application.graph.GraphInterceptor
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleAtFixedRate
import java.nio.file.Path
import java.time.temporal.ChronoUnit

@Component
class SupportService(
    private val interfaceStore: InterfaceStore,
    private val networkManagerService: NetworkManagerService,
    private val agentData: AgentData
) {
    fun updateInterfaces() {
        val interfaceName = agentData.ipv4InterfaceOverride() ?: getDefaultIPv4Interface()

        val networkConfigs = interfaceStore.getAll()
        val newConfigs = networkManagerService.exchangeConfig(networkConfigs.map {
            it.toPair().toExchange()
        })

        val updatedConfigs = newConfigs.map {
            val decodedConfig = it.config.toDecoded(agentData.masterPassword());
            it.copy(
                config = decodedConfig.copy(
                    interfaceSection = decodedConfig.interfaceSection.copy(
                        additionalProperties = decodedConfig.interfaceSection.additionalProperties?.mapValues {
                            it.value.map {
                                it.replace("*currInt", interfaceName)
                            }
                        }
                    )
                )
            )
        }

        updatedConfigs.forEach {
            if (it.config.compareShouldRestart(networkConfigs[it.name])) {
                interfaceStore.update(it.name, it.config)
                restartService(it.name)
            } else if (it.config != networkConfigs[it.name]) {
                interfaceStore.update(it.name, it.config)
                reloadService(it.name)
            }
        }
    }

    fun initCheckPipeline() {
        installPackage()

        interfaceStore.getAll().forEach {
            enableService(Path.of(agentData.folder()), it.key)
        }

        updateInterfaces()
    }
}

@Component
class SupportServiceGraphInterceptor : GraphInterceptor<SupportService> {
    override fun init(value: SupportService?): SupportService? {
        value?.initCheckPipeline()
        return value
    }

    override fun release(value: SupportService?): SupportService? {
        value?.updateInterfaces()
        return value
    }
}

@Component
class ConfigExchangeScheduler(
    private val supportService: SupportService
) {
    @ScheduleAtFixedRate(initialDelay = 1, period = 5, unit = ChronoUnit.MINUTES)
    fun schedule() {
        supportService.updateInterfaces()
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