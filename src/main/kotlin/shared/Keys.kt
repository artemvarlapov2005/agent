package org.matkini.shared

import java.util.Base64
import org.bouncycastle.math.ec.rfc7748.X25519
import org.matkini.ConfigFile

fun getPublic(privateKey: String): String {
    val priv = Base64.getDecoder().decode(privateKey)
    val pub = ByteArray(32)
    X25519.scalarMultBase(priv, 0, pub, 0)
    return Base64.getEncoder().encodeToString(pub)
}

fun ConfigFile.privateKey() = interfaceSection.privateKey