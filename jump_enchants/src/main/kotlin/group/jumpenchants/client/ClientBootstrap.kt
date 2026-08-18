package group.jumpenchants.client

import group.jumpenchants.equipment.EquipmentResolver
import group.jumpenchants.config.JumpConfig
import group.jumpenchants.movement.MovementController
import group.jumpenchants.movement.MovementInput
import group.jumpenchants.network.c2s.InputStatePacket
import group.jumpenchants.network.ModNetwork
import group.jumpenchants.state.JumpStates
import net.minecraft.client.Minecraft
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

object ClientBootstrap {
    fun register() {
        FORGE_BUS.register(ClientEvents)
        FORGE_BUS.register(DebugHud)
    }
}

private object ClientEvents {
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        val state = JumpStates.get(player)

        if (player.tickCount >= state.nextEquipmentCheckTick || state.mode == null) {
            state.switchMode(EquipmentResolver.resolve(player))
            state.nextEquipmentCheckTick = player.tickCount + JumpConfig.equipmentScanIntervalTicks.get()
        }
        val mode = state.mode ?: run {
            state.resetAirborne()
            state.previousGrounded = player.onGround()
            return
        }

        val groundedNow = player.onGround()
        if (state.airborne && groundedNow && state.airborneTicks > JumpConfig.landingGraceTicks.get()) {
            state.resetAirborne()
        }

        val jumpDown = minecraft.options.keyJump.isDown && minecraft.screen == null
        val input = MovementInput(
            player.input.forwardImpulse.toDouble(),
            player.input.leftImpulse.toDouble()
        ).clamped()
        val wasDown = state.jumpDown
        val groundedAtPress = state.previousGrounded || (!state.airborne && groundedNow)

        state.jumpDown = jumpDown
        state.forwardInput = input.forward
        state.strafeInput = input.strafe

        var shouldSend = false
        if (jumpDown && !wasDown) {
            if (groundedAtPress) {
                if (!state.airborne) MovementController.beginFirstJump(player, state, mode)
            } else {
                MovementController.activate(player, state, mode, input)
            }
            shouldSend = true
        } else if (!jumpDown && wasDown) {
            MovementController.release(state)
            shouldSend = true
        } else if (
            (jumpDown || state.abilityActive) &&
            player.tickCount - state.lastHeartbeatTick >= JumpConfig.inputHeartbeatTicks.get()
        ) {
            shouldSend = true
        }

        if (shouldSend) {
            state.lastHeartbeatTick = player.tickCount
            ModNetwork.sendInput(
                InputStatePacket(
                    state.nextClientSequence++,
                    jumpDown,
                    groundedAtPress,
                    input.forward.toFloat(),
                    input.strafe.toFloat()
                )
            )
        }

        MovementController.tick(player, state)
        state.previousGrounded = groundedNow
    }
}
