package org.matkini.service

import org.matkini.ConfigFile
import org.matkini.Reader
import org.matkini.Writer
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import java.nio.file.Path

@ConfigSource("config")
interface AgentData {
    fun path(): String
    fun privateKey(): String
}

@Component
class ConfigFileService(val config: AgentData) {
    fun get() : ConfigFile = Reader.readFile(Path.of(config.path()))

    fun update(configFile : ConfigFile) = Writer.writeToFile(configFile, Path.of(config.path()))
}