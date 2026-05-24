package org.secureime.sect9.crypto.codebook

object EncryptionMarker {
    const val MARKER_OPEN = "⟦"
    const val MARKER_CLOSE = "⟦/⟧"
    const val MODE_CODEBOOK = "CB"
    const val VERSION = "1"
    const val LEGACY_HEADER = "⸝"

    fun wrap(ciphertext: String, contactId: String): String {
        return "$MARKER_OPEN$MODE_CODEBOOK:$VERSION:$contactId$MARKER_CLOSE$ciphertext$MARKER_CLOSE"
    }

    fun unwrap(text: String): Pair<String, String>? {
        if (text.startsWith(MARKER_OPEN)) {
            val afterOpen = text.substring(MARKER_OPEN.length)
            val headerEnd = afterOpen.indexOf(MARKER_CLOSE)
            if (headerEnd > 0) {
                val header = afterOpen.substring(0, headerEnd)
                val parts = header.split(":")
                if (parts.size >= 3 && parts[0] == MODE_CODEBOOK && parts[1] == VERSION) {
                    val contactId = parts[2]
                    val ciphertext = afterOpen.substring(headerEnd + MARKER_CLOSE.length)
                    if (ciphertext.endsWith(MARKER_CLOSE)) {
                        return Pair(contactId, ciphertext.substring(0, ciphertext.length - MARKER_CLOSE.length))
                    }
                }
            }
        }
        if (text.startsWith(LEGACY_HEADER)) {
            val ciphertext = text.substring(LEGACY_HEADER.length)
            return Pair("", ciphertext)
        }
        return null
    }

    fun isEncrypted(text: String): Boolean {
        return text.startsWith(MARKER_OPEN) || text.startsWith(LEGACY_HEADER)
    }

    fun detectMode(text: String): String? {
        return when {
            text.startsWith("⸝·") -> "EMOJI"
            text.startsWith("⸝ ") -> "SM"
            text.startsWith("⸝") -> "CODEBOOK"
            text.startsWith(MARKER_OPEN) -> "CODEBOOK"
            else -> null
        }
    }
}