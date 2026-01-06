package org.matkini.controller

import org.matkini.shared.ClientResult
import org.matkini.action.PutClientAction
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientController(val putClientAction: PutClientAction) {
    private val log = LoggerFactory.getLogger(ClientController::class.java)

    @HttpRoute(method = HttpMethod.PUT, path = "/client")
    @Json
    fun putClient(
       @Json body : PutClientRequest
    ): ClientResult = runCatching {
            log.info("Получен запрос на добавление клиента с pubKey ${body.publicKey} " +
                    "для интерфейса ${body.interfaceName}")
            require(body.publicKey.length == 44)
            putClientAction.put(body)
        }.getOrElse { e ->
            throw e
        } ?: throw HttpServerResponseException.of(503, "Не удалось найти свободный айпи")
}

@Json
data class PutClientRequest(
    val publicKey: String,
    val interfaceName: String)