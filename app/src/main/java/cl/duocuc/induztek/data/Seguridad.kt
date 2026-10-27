package cl.duocuc.induztek.data

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Seguridad {
    fun sal() =
        Base64.getEncoder().encodeToString(ByteArray(16).also { SecureRandom().nextBytes(it) })

    fun hash(clave: String, sal: String): String {
        val spec = PBEKeySpec(clave.toCharArray(), Base64.getDecoder().decode(sal), 60000, 256)
        return try {
            Base64.getEncoder()
                .encodeToString(
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .encoded
                )
        } finally {
            spec.clearPassword()
        }
    }
}
