package org.matkini.service

import org.matkini.ConfigFile
import org.matkini.Reader
import org.matkini.Writer
import org.matkini.shared.AgentData
import org.matkini.shared.ExchangeInterfaceDto
import ru.tinkoff.kora.application.graph.GraphInterceptor
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.text.set

@Component
class InterfaceStore(private val agentData: AgentData) {
    private val interfaces = agentData.interfaces().split(",")
    private var configMapping : MutableMap<String, ConfigFile> = mutableMapOf()

    fun getAllInterfaces() = interfaces

    fun getInterfacesWithConfigs() = configMapping.toMap()

    fun get(interfaceName: String) : ConfigFile? = configMapping[interfaceName]

    fun update(
        interfaceName: String,
        configFile : ConfigFile,
        commit: Boolean = true) {
        configMapping[interfaceName] = configFile

        if (commit) Writer.writeToFile(configFile, getPath(interfaceName))
    }

    private fun getPath(interfaceName: String) = computePath(Path.of(agentData.folder()), interfaceName)
}

@Component
class InterfaceStoreInitParsing(
    val agentData: AgentData
) : GraphInterceptor<InterfaceStore> {
    override fun init(value: InterfaceStore?): InterfaceStore? {
        val interfaces = value?.getAllInterfaces()

        interfaces?.forEach {
            runCatching {
                value.update(
                    it,
                    Reader.readFile(computePath(Path.of(agentData.folder()), it)),
                    false
                )
            }.onFailure { e ->
                e.printStackTrace()
            }
        }

        return value
    }

    override fun release(value: InterfaceStore?): InterfaceStore? {
        return value
    }

}



fun computePath(basePath: Path, interfaceName: String) = basePath.resolve("$interfaceName.conf")