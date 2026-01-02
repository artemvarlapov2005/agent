package org.matkini.service

import ru.tinkoff.kora.common.Component
import java.util.Base64
import org.bouncycastle.math.ec.rfc7748.X25519

@Component
class PublicKeyProvider(val agentData: AgentData) {
    private val publicKey: String? = null

    private fun publicKeyFromPrivate(privateKey: String): String {
        val priv = Base64.getDecoder().decode(privateKey)
        val pub = ByteArray(32)
        X25519.scalarMultBase(priv, 0, pub, 0)
        return Base64.getEncoder().encodeToString(pub)
    }

    fun provide() = publicKey ?: publicKeyFromPrivate(agentData.privateKey())
}