package org.matkini.shared

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("config")
interface AgentData {
    fun folder(): String
    fun masterPassword(): String
    fun serverName(): String
    fun interfaces(): String
    fun networkInterfaceOverride() : String?
}