package org.milad.expense_share

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable(with = AmountSerializer::class)
@JvmInline
value class Amount(val value: Long) : Comparable<Amount> {
    constructor(value: String) : this(if (value.equals("")) 0 else value.toLong())

    operator fun plus(other: Amount): Amount = Amount(value + other.value)

    operator fun minus(other: Amount): Amount = Amount(value - other.value)
    operator fun minus(other: Long): Amount = Amount(value - other)

    operator fun times(multiplier: Long): Amount = Amount(value * multiplier)
    operator fun times(multiplier: Int): Amount = Amount(value * multiplier)

    operator fun div(divisor: Int): Amount = Amount(value / divisor)

    operator fun unaryMinus(): Amount = Amount(-value)

    override operator fun compareTo(other: Amount) = value compareTo other.value
    operator fun compareTo(other: Long) = value compareTo other
    operator fun compareTo(other: Double) = value compareTo other.toLong()
    operator fun compareTo(other: Int) = value compareTo other.toLong()

    fun isPositive(): Boolean = value > 0
    fun isNegative(): Boolean = value < 0
    fun isZero(): Boolean = value == 0L

    fun abs(): Amount = Amount(kotlin.math.abs(value))
}

fun Amount.showSeparate(): String {
    val s = value.toString()
    val isNegative = s.startsWith('-')
    val digits = if (isNegative) s.drop(1) else s

    val separated = digits
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return if (isNegative) "-$separated" else separated
}

fun Long.showSeparate(): String {
    val s = this.toString()
    val isNegative = s.startsWith('-')
    val digits = if (isNegative) s.drop(1) else s

    val separated = digits
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return if (isNegative) "-$separated" else separated
}