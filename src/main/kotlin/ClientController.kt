package org.matkini

import org.matkini.dto.ClientResult
import org.matkini.service.PutClientAction
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientController(val putClientAction: PutClientAction) {

    @HttpRoute(method = HttpMethod.PUT, path = "/client")
    @Json
    fun helloWorld(
        @Query("pubKey") pubKey : String
    ): ClientResult = runCatching {
            putClientAction.put(pubKey)
        }.getOrElse { e ->
            throw e
        } ?: throw HttpServerResponseException.of(503, "Не удалось найти свободный айпи")
}