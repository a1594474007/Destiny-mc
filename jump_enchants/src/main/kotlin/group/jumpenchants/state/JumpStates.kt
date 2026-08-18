package group.jumpenchants.state

import net.minecraft.world.entity.player.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object JumpStates {
    private data class Key(val id: UUID, val clientSide: Boolean)

    private val states = ConcurrentHashMap<Key, JumpState>()

    fun get(player: Player): JumpState =
        states.computeIfAbsent(Key(player.uuid, player.level().isClientSide)) { JumpState() }

    fun remove(player: Player) {
        states.remove(Key(player.uuid, player.level().isClientSide))
    }

    fun remove(id: UUID) {
        states.keys.removeIf { it.id == id }
    }
}

