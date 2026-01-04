package org.matkini

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule

@KoraApp
interface Application : HoconConfigModule, UndertowHttpServerModule, JsonModule, CaffeineCacheModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}