package com.nbottarini.rpgcombat.core

import java.lang.Double.max
import java.lang.Double.min as doubleMin

class Amount(value: Double) {
    private val value = max(value, 0.0)

    constructor(value: Int): this(value.toDouble())

    operator fun minus(other: Amount) = Amount(value - other.value)

    operator fun plus(other: Amount) = Amount(value + other.value)

    operator fun times(scalar: Double) = Amount(value * scalar)

    override fun equals(other: Any?) = other is Amount && other.value == value

    override fun hashCode() = value.hashCode()

    override fun toString() = "Amount($value)"
    fun isZero() = value == 0.0

    companion object {
        fun min(amount: Amount, other: Amount) = Amount(doubleMin(amount.value, other.value))
    }
}

