package org.matkini.service

import org.matkini.ConfigFile
import org.matkini.shared.AgentData
import org.matkini.shared.ExchangeInterfaceDto
import org.matkini.shared.enableService
import org.matkini.shared.getDefaultIPv4Interface
import org.matkini.shared.installPackage
import org.matkini.shared.reloadService
import org.matkini.shared.restartService
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(SupportService::class.java)

    fun updateInterfaces() {
        log.info("Производится обновление интерфейсов")
        val interfaceName = agentData.ipv4InterfaceOverride() ?: getDefaultIPv4Interface()

        log.info("Внешний интерфейс: $interfaceName")

        val networkConfigs = interfaceStore.getAllInterfaces()

        val mapping = interfaceStore.getInterfacesWithConfigs()

        log.info("Прочитано интерфейсов: ${networkConfigs.size}")

        val newConfigs = networkManagerService.exchangeConfig(
            networkConfigs.map {
                ExchangeInterfaceDto(
                    it,
                    mapping[it]
                )
            }
        )

        val updatedConfigs = newConfigs.map {
            log.info("Обработка полученного интерфейса ${it.name}")
            it.copy(
                config = it.config?.copy(
                    interfaceSection = it.config.interfaceSection.copy(
                        additionalProperties = it.config.interfaceSection.additionalProperties?.mapValues {
                            it.value.map {
                                it.replace("*currInt", interfaceName)
                            }
                        }
                    )
                )
            )
        }

        updatedConfigs.forEach {
            if (it.config != null && it.config.compareShouldRestart(mapping[it.name]) == true) {
                log.info("Полный перезапуск для интерфейса: ${it.name}")
                interfaceStore.update(it.name, it.config)
                restartService(it.name)
                log.info("Полный перезапуск для интерфейса завершен: ${it.name}")
            } else if (it.config != null && it.config != mapping[it.name]) {
                log.info("Обновление для интерфейса: ${it.name}")
                interfaceStore.update(it.name, it.config)
                reloadService(it.name)
                log.info("Обновление для интерфейса завершено: ${it.name}")
            }
        }

        log.info("Завершено обновление интерфейсов")
    }

    fun initCheckPipeline() {
        log.info("Начата инициализация агента")
        installPackage()
        log.info("AWG успешно установлено")
        interfaceStore.getAllInterfaces().forEach {
            log.info("Включаю интерфейс $it")
            enableService(Path.of(agentData.folder()), it)
            restartService(it)
            log.info("Включен интерфейс $it")
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

    override fun release(value: SupportService?): SupportService? = runCatching {
        value?.updateInterfaces()
        return value
    }.getOrElse { value }

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