package group.jumpenchants.movement

import group.jumpenchants.config.JumpConfig
import group.jumpenchants.config.BlinkSettings
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object BlinkService {
    fun hasGroundClearance(player: Player, settings: BlinkSettings): Boolean {
        if (settings.minimumGroundClearance <= 0.0) return true
        val start = player.position().add(0.0, 0.01, 0.0)
        val end = start.add(0.0, -(settings.minimumGroundClearance + 0.01), 0.0)
        val hit = player.level().clip(
            ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)
        )
        return hit.type == HitResult.Type.MISS || start.y - hit.location.y >= settings.minimumGroundClearance
    }

    fun tryBlink(player: ServerPlayer, settings: BlinkSettings): Boolean {
        val level = player.serverLevel()
        val direction = player.lookAngle.normalize()
        val configuredDistance = settings.distance
        val eyeStart = player.eyePosition
        val eyeEnd = eyeStart.add(direction.scale(configuredDistance))
        val hit = level.clip(ClipContext(eyeStart, eyeEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
        val unobstructedDistance = if (hit.type == HitResult.Type.MISS) {
            configuredDistance
        } else {
            eyeStart.distanceTo(hit.location).minus(settings.wallMargin).coerceAtLeast(0.0)
        }

        val start = player.position()
        var distance = unobstructedDistance
        val step = settings.collisionSearchStep
        while (distance >= step) {
            val candidate = start.add(direction.scale(distance))
            val blockPos = BlockPos.containing(candidate)
            val movedBox = player.boundingBox.move(candidate.subtract(start))
            if (
                level.hasChunkAt(blockPos) &&
                level.worldBorder.isWithinBounds(movedBox) &&
                level.noCollision(player, movedBox)
            ) {
                player.connection.teleport(candidate.x, candidate.y, candidate.z, player.yRot, player.xRot)
                player.deltaMovement = Vec3(
                    0.0,
                    MovementPhysics.clampVertical(
                        settings.verticalVelocity,
                        JumpConfig.physicsTuning()
                    ),
                    0.0
                )
                player.fallDistance = 0.0f
                player.hurtMarked = true
                return true
            }
            distance -= step
        }
        return false
    }
}
