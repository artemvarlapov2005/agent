package org.matkini.service

import org.matkini.ConfigFile
import org.matkini.Reader
import org.matkini.Writer
import ru.tinkoff.kora.application.graph.GraphInterceptor
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.text.get

@ConfigSource("config")
interface AgentData {
    fun path(): String
    fun masterPassword(): String
    fun serverName(): String
}

@Component
class ConfigFileService(val config: AgentData) {
    fun get() : ConfigFile = Reader.readFile(Path.of(config.path()))

    fun update(configFile : ConfigFile) = Writer.writeToFile(configFile, Path.of(config.path()))

    fun updateIfChanged(newConfig: ConfigFile) {
        if (newConfig != get()) {
            update(newConfig)
        }
    }

    fun createFile() = Path.of(config.path()).createFile()

    fun configExists() = Path.of(config.path()).exists()
}

@Component
class ConfigFileServiceInitExchanger(
    val networkManagerService: NetworkManagerService,
) : GraphInterceptor<ConfigFileService> {

    override fun init(value: ConfigFileService): ConfigFileService = runCatching {
        if (!value.configExists()) {
            value.createFile()
            value.also { networkManagerService.exchangeConfig(null) }
        } else {
            value.also { it.updateIfChanged(networkManagerService.exchangeConfig(value.get())) }
        }
    }.getOrElse { e ->
        throw IllegalStateException("Unable to get current config from server", e)
    }


    override fun release(value: ConfigFileService): ConfigFileService {
        return value
    }
}

