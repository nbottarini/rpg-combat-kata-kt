package com.nbottarini.rpgcombat.core

import com.nbottarini.rpgcombat.core.character.Attackable

class Thing(initialHealth: Amount): Attackable {
    var health = initialHealth
        private set
    val isDestroyed get() = health.isZero()

    override fun receiveDamage(damage: Amount) {
        health -= damage
    }
}
