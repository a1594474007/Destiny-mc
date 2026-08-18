package group.jumpenchants.client

import group.jumpenchants.config.JumpConfig
import group.jumpenchants.movement.JobType
import group.jumpenchants.state.JumpStates
import net.minecraft.client.player.LocalPlayer
import net.minecraft.util.Mth

object ViewLockController {
    private const val TURN_SCALE = 0.15

    @JvmStatic
    fun applyLockedTurn(player: LocalPlayer, mouseX: Double, mouseY: Double): Boolean {
        val state = JumpStates.get(player)
        val mode = state.mode
        if (mode == null || mode.isBlink || !state.abilityActive || !state.specialActivationUsed) {
            return false
        }

        val settings = when (mode.jobType) {
            JobType.TITAN -> ViewLockSettings(
                JumpConfig.titanLockViewDuringAbility.get(),
                JumpConfig.titanViewLockYawToleranceDegrees.get(),
                JumpConfig.titanViewLockPitchToleranceDegrees.get()
            )
            JobType.WARLOCK -> ViewLockSettings(
                JumpConfig.warlockLockViewDuringAbility.get(),
                JumpConfig.warlockViewLockYawToleranceDegrees.get(),
                JumpConfig.warlockViewLockPitchToleranceDegrees.get()
            )
            JobType.HUNTER -> ViewLockSettings.DISABLED
        }
        if (!settings.enabled) {
            return false
        }

        val requestedYaw = player.yRot + (mouseX * TURN_SCALE).toFloat()
        val requestedPitch = player.xRot + (mouseY * TURN_SCALE).toFloat()
        val yawOffset = Mth.wrapDegrees(requestedYaw - state.capturedViewYaw)
            .coerceIn(-settings.yawTolerance, settings.yawTolerance)
        val pitchOffset = (requestedPitch - state.capturedViewPitch)
            .coerceIn(-settings.pitchTolerance, settings.pitchTolerance)
        val targetYaw = state.capturedViewYaw + yawOffset
        val targetPitch = (state.capturedViewPitch + pitchOffset).coerceIn(-90.0f, 90.0f)
        val appliedYaw = Mth.wrapDegrees(targetYaw - player.yRot)
        val appliedPitch = targetPitch - player.xRot
        player.xRot = targetPitch
        player.yRot += appliedYaw
        player.xRotO = Mth.clamp(player.xRotO + appliedPitch, -90.0f, 90.0f)
        player.yRotO += appliedYaw
        player.vehicle?.onPassengerTurned(player)
        return true
    }

    private data class ViewLockSettings(
        val enabled: Boolean,
        val yawTolerance: Float,
        val pitchTolerance: Float
    ) {
        constructor(enabled: Boolean, yawTolerance: Double, pitchTolerance: Double) : this(
            enabled,
            yawTolerance.toFloat(),
            pitchTolerance.toFloat()
        )

        companion object {
            val DISABLED = ViewLockSettings(false, 0.0f, 0.0f)
        }
    }
}
