package com.nbottarini.rpgcombat.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ThingTest {
    @Test
    fun `gets destroyed when receives more damage than remaining health`() {
        val thing = Thing(initialHealth = Amount(500))

        thing.receiveDamage(Amount(600))

        assertThat(thing.isDestroyed).isTrue
    }
}
