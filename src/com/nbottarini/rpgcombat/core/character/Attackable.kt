package com.nbottarini.rpgcombat.core.character

import com.nbottarini.rpgcombat.core.Amount

interface Attackable {
    fun receiveDamage(damage: Amount)
}
