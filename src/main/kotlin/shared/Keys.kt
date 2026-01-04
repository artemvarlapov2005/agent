package org.matkini.util

import java.util.Base64
import org.bouncycastle.math.ec.rfc7748.X25519
import org.matkini.ConfigFile
import java.security.SecureRandom

fun getPublic(privateKey: String): String {
    val priv = Base64.getDecoder().decode(privateKey)
    val pub = ByteArray(32)
    X25519.scalarMultBase(priv, 0, pub, 0)
    return Base64.getEncoder().encodeToString(pub)
}

fun getPrivateKey(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return Base64.getEncoder().encodeToString(bytes)
}

fun ConfigFile.privateKey() = interfaceSection.privateKey