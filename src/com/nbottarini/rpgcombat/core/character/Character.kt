package com.nbottarini.rpgcombat.core.character

import com.nbottarini.rpgcombat.core.Amount
import com.nbottarini.rpgcombat.core.Amount.Companion.min

class Character private constructor (private val attackRangeInMeters: Double): Attackable {
    private var _factions = mutableSetOf<String>()
    val factions get() = _factions.toList()
    var level = 1
        private set
    var health = Amount(1000)
        private set(value) {
            field = min(value, Amount(1000))
        }
    val isDead get() = health.isZero()

    fun attack(victim: Attackable, damage: Amount, distanceInMeters: Double = 0.0) {
        if (!canAttack(victim, distanceInMeters)) return
        failIfAttackingItself(victim)
        victim.receiveDamage(calculateEffectiveDamage(victim, damage))
    }

    private fun canAttack(victim: Attackable, distanceInMeters: Double): Boolean {
        if (outOfRange(distanceInMeters)) return false
        if (victim is Character && isAlly(victim)) return false
        return true
    }

    private fun outOfRange(distanceInMeters: Double) = distanceInMeters > attackRangeInMeters

    private fun calculateEffectiveDamage(victim: Attackable, damage: Amount): Amount {
        if (victim !is Character) return damage
        if (victim.level - this.level >= 5) return damage * 0.5
        if (this.level - victim.level >= 5) return damage * 1.5
        return damage
    }

    private fun failIfAttackingItself(victim: Attackable) {
        if (victim == this) throw CannotAttackItselfError()
    }

    override fun receiveDamage(damage: Amount) {
        health -= damage
    }

    fun heal(healing: Amount) {
        heal(this, healing)
    }

    fun heal(other: Character, healing: Amount) {
        failIfNotItselfOrAlly(other)
        failIfDead(other)
        other.health += healing
    }

    private fun failIfNotItselfOrAlly(other: Character) {
        if (other != this && !isAlly(other)) throw CannotHealError()
    }

    private fun failIfDead(character: Character) {
        if (character.isDead) throw DeadCharacterError()
    }

    fun increaseLevel() {
        level++
    }

    fun join(faction: String) {
        _factions.add(faction)
    }

    fun leave(faction: String) {
        _factions.remove(faction)
    }

    fun isAlly(other: Character) = _factions.intersect(other._factions).isNotEmpty()

    companion object {
        fun melee() = Character(2.0)
        fun ranged() = Character(20.0)
    }
}
