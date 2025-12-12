package org.milad.expense_share

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object AmountSerializer : KSerializer<Amount> {
    override val descriptor = PrimitiveSerialDescriptor("Amount", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Amount) {
        encoder.encodeLong(value.value)
    }

    override fun deserialize(decoder: Decoder): Amount {
        return Amount(decoder.decodeLong())
    }
}