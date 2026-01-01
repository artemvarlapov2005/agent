package org.matkini

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import java.nio.charset.StandardCharsets

@Component
@HttpController
class HelloWorldController {

    @HttpRoute(method = HttpMethod.GET, path = "/hello/world")
    fun helloWorld(): HttpServerResponse {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello World"))
    }
}