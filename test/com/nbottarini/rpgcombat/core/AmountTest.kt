package com.nbottarini.rpgcombat.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AmountTest {
    @Test
    fun `min amount is zero`() {
        assertThat(Amount(-50)).isEqualTo(Amount(0))
    }
}
