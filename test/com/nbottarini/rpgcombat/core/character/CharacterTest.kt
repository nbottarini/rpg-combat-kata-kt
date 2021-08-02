@file:Suppress("ClassName")

package com.nbottarini.rpgcombat.core.character

import com.nbottarini.rpgcombat.core.Amount
import com.nbottarini.rpgcombat.core.Thing
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CharacterTest {
    @Test
    fun `initial health is 1000`() {
        assertThat(character().health).isEqualTo(Amount(1000))
    }

    @Test
    fun `starts at level 1`() {
        assertThat(character().level).isEqualTo(1)
    }

    @Nested
    inner class attacking {
        @Test
        fun `attacking other character reduces health by given damage`() {
            val remainingHealth = victim.health
            val damage = Amount(500)

            attacker.attack(victim, damage)

            assertThat(victim.health).isEqualTo(remainingHealth - damage)
        }

        @Test
        fun `character dies when receiving damage greater than remaining health`() {
            attacker.attack(victim, victim.health)

            assertThat(victim.health.isZero()).isTrue
            assertThat(victim.isDead).isTrue
        }

        @Test
        fun `cannot attack itself`() {
            assertThrows<CannotAttackItselfError> {
                attacker.attack(attacker, someAmount)
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [5, 6])
        fun `do 50 percent less damage if victim is 5 or more levels above the attacker level`(levelsAbove: Int) {
            val victim = characterAt(level = attacker.level + levelsAbove)
            val initialHealth = victim.health
            val damage = Amount(100)

            attacker.attack(victim, damage)

            val effectiveDamage = initialHealth - victim.health
            assertThat(effectiveDamage).isEqualTo(damage * 0.5)
        }

        @ParameterizedTest
        @ValueSource(ints = [5, 6])
        fun `do 50 percent more damage if attacker is 5 or more levels above the victim level`(levelsAbove: Int) {
            val attacker = characterAt(level = victim.level + levelsAbove)
            val initialHealth = victim.health
            val damage = Amount(100)

            attacker.attack(victim, damage)

            val effectiveDamage = initialHealth - victim.health
            assertThat(effectiveDamage).isEqualTo(damage * 1.5)
        }

        @Test
        fun `melee fighters have an attack range of 2 meters`() {
            val initialHealth = victim.health
            val distanceOutOfRange = 2.1

            Character.melee().attack(victim, someAmount, distanceOutOfRange)

            assertThat(victim.health).isEqualTo(initialHealth)
        }

        @Test
        fun `ranged fighters have an attack range of 20 meters`() {
            val initialHealth = victim.health
            val distanceOutOfRange = 20.1

            Character.ranged().attack(victim, someAmount, distanceOutOfRange)

            assertThat(victim.health).isEqualTo(initialHealth)
        }
    }

    @Nested
    inner class healing {
        @Test
        fun `can heal`() {
            val wounded = character(health = 700)

            wounded.heal(Amount(200))

            assertThat(wounded.health).isEqualTo(Amount(900))
        }

        @Test
        fun `max health is 1000`() {
            val wounded = character(health = 900)

            wounded.heal(Amount(200))

            assertThat(wounded.health).isEqualTo(Amount(1000))
        }

        @Test
        fun `dead characters cannot be healed`() {
            assertThrows<DeadCharacterError> {
                dead.heal(someAmount)
            }
        }

        @Test
        fun `cannot heal other characters`() {
            val other = character()

            assertThrows<CannotHealError> {
                character.heal(other, someAmount)
            }
        }
    }

    @Nested
    inner class factions {
        @Test
        fun `can join a faction`() {
            character.join("faction1")
            character.join("faction2")

            assertThat(character.factions).containsExactlyInAnyOrder("faction1", "faction2")
        }

        @Test
        fun `joining an already joined faction does nothing`() {
            character.join("faction1")
            character.join("faction1")

            assertThat(character.factions).containsExactly("faction1")
        }

        @Test
        fun `can leave a faction`() {
            val character = characterIn("faction1", "faction2")

            character.leave("faction1")

            assertThat(character.factions).containsExactly("faction2")
        }

        @Test
        fun `allies belong to same faction`() {
            val character1 = characterIn("faction1", "faction2")
            val character2 = characterIn("faction2")

            assertThat(character1.isAlly(character2)).isTrue
            assertThat(character2.isAlly(character1)).isTrue
        }

        @Test
        fun `allies cannot deal damage to one another`() {
            val ally1 = characterIn("faction1")
            val ally2 = characterIn("faction1")
            val ally2InitialHealth = ally2.health

            ally1.attack(ally2, someAmount)

            assertThat(ally2.health).isEqualTo(ally2InitialHealth)
        }

        @Test
        fun `allies can heal one another`() {
            val character = characterIn("faction1")
            val woundedAlly = character(health = 500).also { it.join("faction1") }

            character.heal(woundedAlly, Amount(200))

            assertThat(woundedAlly.health).isEqualTo(Amount(700))
        }

        @Test
        fun `cannot heal dead ally`() {
            val character = characterIn("faction1")
            val deadAlly = dead.also { it.join("faction1") }

            assertThrows<DeadCharacterError> { character.heal(deadAlly, someAmount) }
        }
    }

    @Nested
    inner class things {
        @Test
        fun `can attack things reducing health with given damage`() {
            val thing = Thing(initialHealth = Amount(2000))

            attacker.attack(thing, Amount(500))

            assertThat(thing.health).isEqualTo(Amount(1500))
        }
    }

    private fun characterAt(level: Int) = character().also { c ->
        repeat(level - 1) { c.increaseLevel() }
    }

    private fun characterIn(vararg faction: String) = character().also {
        faction.forEach { f -> it.join(f) }
    }

    private fun character(health: Int) = character().also {
        attacker.attack(it, it.health - Amount(health))
    }

    private fun character() = Character.melee()

    private val someAmount = Amount(200)
    private val character = character()
    private val attacker = character
    private val victim = character()
    private val dead = character().also { attacker.attack(it, it.health) }
}
