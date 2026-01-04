package org.matkini.service

import org.matkini.ConfigFile
import org.matkini.Reader
import org.matkini.Writer
import org.matkini.shared.AgentData
import ru.tinkoff.kora.application.graph.GraphInterceptor
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import java.nio.file.Path
import kotlin.io.path.createFile

@Component
class InterfaceStore(private val agentData: AgentData) {
    private val diskData: MutableMap<String, ConfigFile> =
        agentData.interfaces().split(",").associate {
            it to Reader.readFile(getPath(it))
    }.toMutableMap()

    fun getAll() = diskData.toMap()

    fun get(interfaceName: String) = diskData[interfaceName]

    fun update(
        interfaceName: String,
        configFile : ConfigFile) =
        Writer.writeToFile(configFile, getPath(interfaceName)).also {
            diskData[interfaceName] = configFile
        }

    private fun getPath(interfaceName: String) = computePath(Path.of(agentData.folder()), interfaceName)
}

fun computePath(basePath: Path, interfaceName: String) = basePath.resolve("$interfaceName.conf")