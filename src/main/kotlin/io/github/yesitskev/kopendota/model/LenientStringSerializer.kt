package io.github.yesitskev.kopendota.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

/**
 * Reads a JSON string or number alike into a [String], and writes one back out as a string.
 *
 * A handful of OpenDota members carry a key that is a number in one match and a string in the next — the rune type
 * in `runes_log`, the chat wheel line in `chat`, the objective key in `objectives` — and the published schema does
 * not always agree with what the API sends. Decoding those as plain strings would fail on whichever form the schema
 * did not predict, so they go through this instead.
 */
internal object LenientStringSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.github.yesitskev.kopendota.model.LenientString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String =
        when (decoder) {
            is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content
            else -> decoder.decodeString()
        }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
