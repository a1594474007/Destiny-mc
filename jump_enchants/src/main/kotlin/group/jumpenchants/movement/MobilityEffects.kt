package group.jumpenchants.movement

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

object MobilityEffects {
    fun emit(player: ServerPlayer, mode: MobilityMode, origin: Vec3) {
        val level = player.serverLevel()
        when {
            mode.isBlink -> {
                level.playSound(
                    null,
                    origin.x,
                    origin.y,
                    origin.z,
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    0.8f,
                    1.15f
                )
                level.playSound(
                    null,
                    player.x,
                    player.y,
                    player.z,
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    0.8f,
                    1.35f
                )
                level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y + 1.0, origin.z, 24, 0.3, 0.6, 0.3, 0.25)
                level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    player.x,
                    player.y + 1.0,
                    player.z,
                    20,
                    0.3,
                    0.6,
                    0.3,
                    0.12
                )
            }
            mode.jobType == JobType.TITAN -> {
                level.playSound(
                    null,
                    player.x,
                    player.y,
                    player.z,
                    SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.PLAYERS,
                    0.45f,
                    1.55f
                )
                level.sendParticles(ParticleTypes.CLOUD, player.x, player.y + 0.1, player.z, 8, 0.22, 0.08, 0.22, 0.02)
            }
            mode.jobType == JobType.WARLOCK -> {
                level.playSound(
                    null,
                    player.x,
                    player.y,
                    player.z,
                    SoundEvents.PHANTOM_FLAP,
                    SoundSource.PLAYERS,
                    0.35f,
                    1.45f
                )
                level.sendParticles(
                    ParticleTypes.END_ROD,
                    player.x,
                    player.y + 0.7,
                    player.z,
                    6,
                    0.18,
                    0.28,
                    0.18,
                    0.015
                )
            }
            else -> {
                level.playSound(
                    null,
                    player.x,
                    player.y,
                    player.z,
                    SoundEvents.SLIME_JUMP_SMALL,
                    SoundSource.PLAYERS,
                    0.55f,
                    1.35f
                )
                level.sendParticles(ParticleTypes.POOF, player.x, player.y + 0.1, player.z, 8, 0.22, 0.08, 0.22, 0.02)
            }
        }
    }
}
