package org.matkini

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.annotation.HttpController

@Component
@HttpController
class HelloWorldController(val configFileProvider: ConfigFileProvider) {

    @HttpRoute(method = HttpMethod.PUT, path = "/client")
    fun helloWorld(
        @Query("pubKey") pubKey : String
    ): String {
        return configFileProvider.get().interfaceSection.privateKey!!;
    }
}