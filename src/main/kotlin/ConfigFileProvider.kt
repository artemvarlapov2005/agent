package org.matkini

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import java.nio.file.Path

@ConfigSource("config")
interface ConfigFileProviderConfig {
    fun path(): String
    fun privateKey(): String
}

@Component
class ConfigFileProvider(val config: ConfigFileProviderConfig) {
    var configFile : ConfigFile? = null

    private fun parse() : ConfigFile {
        configFile = Reader.readFile(Path.of(config.path()))

        return configFile!!
    }

    fun get() : ConfigFile = configFile ?: parse()
}