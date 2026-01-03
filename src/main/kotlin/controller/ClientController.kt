package org.matkini.controller

import org.matkini.ConfigFile
import org.matkini.dto.ClientResult
import org.matkini.action.PutClientAction
import org.matkini.service.ConfigFileService
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientController(val putClientAction: PutClientAction) {

    @HttpRoute(method = HttpMethod.PUT, path = "/client")
    @Json
    fun putClient(
       @Json body : PutClientRequest
    ): ClientResult = runCatching {
            putClientAction.put(body.publicKey)
        }.getOrElse { e ->
            throw e
        } ?: throw HttpServerResponseException.of(503, "Не удалось найти свободный айпи")
}

@Json
data class PutClientRequest(val publicKey: String)