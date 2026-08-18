package group.jumpenchants.network.c2s

import group.jumpenchants.config.JumpConfig
import group.jumpenchants.equipment.EquipmentResolver
import group.jumpenchants.movement.MobilityEffects
import group.jumpenchants.movement.MovementController
import group.jumpenchants.movement.MovementInput
import group.jumpenchants.network.ModNetwork
import group.jumpenchants.network.s2c.ActionResultPacket
import group.jumpenchants.state.JumpState
import group.jumpenchants.state.JumpStates
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier
import kotlin.math.ceil

data class InputStatePacket(
    val sequence: Int,
    val jumpDown: Boolean,
    val groundedAtPress: Boolean,
    val forward: Float,
    val strafe: Float
) {
    fun encode(buffer: FriendlyByteBuf) {
        buffer.writeVarInt(sequence)
        buffer.writeBoolean(jumpDown)
        buffer.writeBoolean(groundedAtPress)
        buffer.writeFloat(forward)
        buffer.writeFloat(strafe)
    }

    companion object {
        fun decode(buffer: FriendlyByteBuf) = InputStatePacket(
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readFloat(),
            buffer.readFloat()
        )
    }
}

object InputStatePacketHandler {
    fun handle(packet: InputStatePacket, contextSupplier: Supplier<NetworkEvent.Context>) {
        val context = contextSupplier.get()
        context.enqueueWork {
            val player = context.sender ?: return@enqueueWork
            val state = JumpStates.get(player)
            MovementController.sampleServerHorizontalMotion(player, state)
            if (packet.sequence <= state.lastInputSequence || !allowPacket(player.tickCount, state)) {
                return@enqueueWork
            }
            state.lastInputSequence = packet.sequence

            val mode = EquipmentResolver.resolve(player)
            if (mode == null) {
                state.switchMode(null)
                sendResult(player, packet.sequence, false, false, state, -1, false)
                return@enqueueWork
            }
            state.switchMode(mode)

            val wasDown = state.jumpDown
            state.jumpDown = packet.jumpDown
            state.forwardInput = packet.forward.toDouble().coerceIn(-1.0, 1.0)
            state.strafeInput = packet.strafe.toDouble().coerceIn(-1.0, 1.0)

            var result = MovementController.ActionResult.ACCEPTED
            var specialAction = false
            val oldPosition = player.position()
            if (packet.jumpDown && !wasDown) {
                if (packet.groundedAtPress || !state.airborne) {
                    MovementController.beginFirstJump(player, state, mode)
                } else {
                    specialAction = true
                    result = MovementController.activate(
                        player,
                        state,
                        mode,
                        MovementInput(state.forwardInput, state.strafeInput)
                    )
                }
            } else if (!packet.jumpDown && wasDown) {
                MovementController.release(state)
            }

            val accepted = result != MovementController.ActionResult.REJECTED
            val teleported = specialAction && mode.isBlink && accepted && oldPosition != player.position()
            if (specialAction && result == MovementController.ActionResult.ACCEPTED) {
                MobilityEffects.emit(player, mode, oldPosition)
            }
            if (packet.jumpDown != wasDown) {
                sendResult(player, packet.sequence, accepted, specialAction, state, mode.ordinal, teleported)
            }
        }
        context.packetHandled = true
    }

    private fun sendResult(
        player: net.minecraft.server.level.ServerPlayer,
        sequence: Int,
        accepted: Boolean,
        specialAction: Boolean,
        state: JumpState,
        modeId: Int,
        teleported: Boolean
    ) {
        ModNetwork.sendToPlayer(
            player,
            ActionResultPacket(
                sequence,
                accepted,
                specialAction,
                modeId,
                state.extraJumpsUsed,
                state.budgetRemaining,
                state.abilityActive,
                state.specialActivationUsed,
                player.deltaMovement,
                teleported,
                player.position()
            )
        )
    }

    private fun allowPacket(tickCount: Int, state: JumpState): Boolean {
        val windowTicks = JumpConfig.packetRateWindowTicks.get()
        if (tickCount - state.packetWindowStartTick >= windowTicks) {
            state.packetWindowStartTick = tickCount
            state.packetsInWindow = 0
        }
        state.packetsInWindow++
        val allowedInWindow = ceil(
            JumpConfig.inputPacketsPerSecond.get() * windowTicks / 20.0
        ).toInt().coerceAtLeast(1)
        return state.packetsInWindow <= allowedInWindow
    }
}
