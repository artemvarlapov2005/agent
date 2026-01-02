package org.matkini.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
@JvmRecord
data class ClientResult(
    val pubKey: String,
    val ip: String,
)
