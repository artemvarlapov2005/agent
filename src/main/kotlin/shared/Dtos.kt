package org.matkini.shared

import org.matkini.ConfigFile
import ru.tinkoff.kora.json.common.annotation.Json

@Json
@JvmRecord
data class ClientResult(
    val pubKey: String,
    val ip: String,
)

data class ExchangeInterfaceDto(val name : String, val config : ConfigFile?)