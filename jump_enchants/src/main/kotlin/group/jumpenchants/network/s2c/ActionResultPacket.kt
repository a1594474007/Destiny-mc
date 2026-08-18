package group.jumpenchants.network.s2c

import group.jumpenchants.config.JumpConfig
import group.jumpenchants.movement.MobilityMode
import group.jumpenchants.state.JumpStates
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

data class ActionResultPacket(
    val sequence: Int,
    val accepted: Boolean,
    val specialAction: Boolean,
    val modeId: Int,
    val extraJumpsUsed: Int,
    val budgetRemaining: Int,
    val abilityActive: Boolean,
    val specialActivationUsed: Boolean,
    val motion: Vec3,
    val teleported: Boolean,
    val position: Vec3
) {
    fun encode(buffer: FriendlyByteBuf) {
        buffer.writeVarInt(sequence)
        buffer.writeBoolean(accepted)
        buffer.writeBoolean(specialAction)
        buffer.writeVarInt(modeId)
        buffer.writeVarInt(extraJumpsUsed)
        buffer.writeVarInt(budgetRemaining)
        buffer.writeBoolean(abilityActive)
        buffer.writeBoolean(specialActivationUsed)
        buffer.writeDouble(motion.x)
        buffer.writeDouble(motion.y)
        buffer.writeDouble(motion.z)
        buffer.writeBoolean(teleported)
        buffer.writeDouble(position.x)
        buffer.writeDouble(position.y)
        buffer.writeDouble(position.z)
    }

    companion object {
        fun decode(buffer: FriendlyByteBuf) = ActionResultPacket(
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
            buffer.readBoolean(),
            Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
        )
    }
}

object ActionResultPacketHandler {
    fun handle(packet: ActionResultPacket, contextSupplier: Supplier<NetworkEvent.Context>) {
        val context = contextSupplier.get()
        context.enqueueWork { apply(packet) }
        context.packetHandled = true
    }

    private fun apply(packet: ActionResultPacket) {
        val player = Minecraft.getInstance().player ?: return
        val state = JumpStates.get(player)
        MobilityMode.fromNetworkId(packet.modeId)?.let(state::switchMode)
        state.extraJumpsUsed = packet.extraJumpsUsed
        state.budgetRemaining = packet.budgetRemaining
        state.abilityActive = packet.abilityActive
        state.specialActivationUsed = packet.specialActivationUsed

        if (packet.teleported) {
            MobilityMode.fromNetworkId(packet.modeId)?.let { mode ->
                val cooldown = JumpConfig.blink(mode).cooldownTicks
                state.setBlinkCooldown(mode.jobType, player.tickCount + cooldown)
            }
            player.setPos(packet.position.x, packet.position.y, packet.position.z)
            player.deltaMovement = packet.motion
            return
        }

        if (packet.specialAction && !packet.accepted) {
            player.deltaMovement = packet.motion
        }
    }
}
