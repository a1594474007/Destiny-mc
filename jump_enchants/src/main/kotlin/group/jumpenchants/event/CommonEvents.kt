package group.jumpenchants.event

import group.jumpenchants.equipment.EquipmentResolver
import group.jumpenchants.config.JumpConfig
import group.jumpenchants.movement.MovementController
import group.jumpenchants.state.JumpStates
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object CommonEvents {
    @SubscribeEvent
    fun onLivingJump(event: LivingEvent.LivingJumpEvent) {
        val player = event.entity as? Player ?: return
        val mode = EquipmentResolver.resolve(player) ?: return
        val state = JumpStates.get(player)
        if (player is ServerPlayer) MovementController.sampleServerHorizontalMotion(player, state)
        MovementController.beginFirstJump(player, state, mode, resetForVanillaJump = true)
    }

    @SubscribeEvent
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        val player = event.player as? ServerPlayer ?: return
        val state = JumpStates.get(player)
        MovementController.sampleServerHorizontalMotion(player, state)

        if (player.tickCount >= state.nextEquipmentCheckTick || state.mode == null) {
            state.switchMode(EquipmentResolver.resolve(player))
            state.nextEquipmentCheckTick = player.tickCount + JumpConfig.equipmentScanIntervalTicks.get()
        }
        if (state.mode == null) {
            state.resetAirborne()
            return
        }

        val grounded = player.onGround()
        if (state.airborne && grounded && state.airborneTicks > JumpConfig.landingGraceTicks.get()) {
            state.resetAirborne()
        } else {
            MovementController.tick(player, state)
        }
        state.previousGrounded = grounded
    }

    @SubscribeEvent
    fun onLogout(event: PlayerEvent.PlayerLoggedOutEvent) {
        JumpStates.remove(event.entity)
    }

    @SubscribeEvent
    fun onChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        JumpStates.remove(event.entity)
    }

    @SubscribeEvent
    fun onRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        JumpStates.remove(event.entity)
    }

    @SubscribeEvent
    fun onClone(event: PlayerEvent.Clone) {
        JumpStates.remove(event.original.uuid)
        JumpStates.remove(event.entity)
    }
}
