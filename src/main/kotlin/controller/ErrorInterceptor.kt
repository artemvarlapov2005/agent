package org.matkini.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor
import ru.tinkoff.kora.http.server.common.HttpServerModule
import ru.tinkoff.kora.http.server.common.HttpServerRequest
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeoutException

@Tag(HttpServerModule::class)
@Component
class ErrorInterceptor : HttpServerInterceptor {

    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain
    ): CompletionStage<HttpServerResponse> {
        return chain.process(context, request).exceptionally { e ->
            val error = if (e is CompletionException) e.cause ?: e else e
            if (error is HttpServerResponseException) {
                return@exceptionally error
            }

            val body = HttpBody.plaintext(error.message)
            when (error) {
                is IllegalArgumentException -> HttpServerResponse.of(400, body)
                is IllegalStateException -> HttpServerResponse.of(422, body)
                is TimeoutException -> HttpServerResponse.of(408, body)
                else -> HttpServerResponse.of(500, body)
            }
        }
    }
}