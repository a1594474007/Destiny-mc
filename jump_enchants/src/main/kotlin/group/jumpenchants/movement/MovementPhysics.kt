package group.jumpenchants.movement

import kotlin.math.max
import kotlin.math.min

data class PhysicsTuning(
    val maxUpwardVelocity: Double = 3.0,
    val maxDownwardVelocity: Double = 4.0,
    val titanFallingVelocityFloor: Double = -0.02,
    val titanFallingRecoveryFraction: Double = 0.75,
    val warlockFallingRecoveryFraction: Double = 0.65
)

object MovementPhysics {
    fun hunterFirstJumpVelocity(currentY: Double, agility: Double, impulsePerPoint: Double, tuning: PhysicsTuning) =
        clampVertical(currentY + agility * impulsePerPoint, tuning)

    fun hunterHoldVelocity(currentY: Double, impulsePerTick: Double, tuning: PhysicsTuning) =
        clampVertical(currentY + impulsePerTick, tuning)

    fun hunterExtraJumpVelocity(verticalImpulse: Double, tuning: PhysicsTuning) =
        clampVertical(verticalImpulse, tuning)

    fun titanSustainVelocity(
        currentY: Double,
        directionalAccelerationY: Double,
        falling: Boolean,
        tuning: PhysicsTuning
    ): Double = clampVertical(
        if (falling) min(tuning.titanFallingVelocityFloor, currentY + directionalAccelerationY)
        else currentY + directionalAccelerationY,
        tuning
    )

    fun titanVerticalCorrection(
        currentY: Double,
        lookY: Double,
        fallingActivation: Boolean,
        hoverDescentSpeed: Double,
        lookVerticalSpeed: Double,
        maxCorrection: Double,
        tuning: PhysicsTuning
    ): Double {
        val target = (-hoverDescentSpeed + min(0.0, lookY) * lookVerticalSpeed).let {
            if (fallingActivation) min(it, tuning.titanFallingVelocityFloor) else it
        }
        return (target - currentY)
            .coerceIn(-maxCorrection.coerceAtLeast(0.0), maxCorrection.coerceAtLeast(0.0))
    }

    fun warlockActivationVelocity(
        currentY: Double,
        profile: MobilityProfile,
        falling: Boolean,
        tuning: PhysicsTuning
    ): Double = clampVertical(
        if (falling) {
            cushionedFallingVelocity(
                currentY,
                profile.verticalMomentumFactor,
                profile.fallingCushion,
                tuning.warlockFallingRecoveryFraction,
                0.0
            )
        } else {
            currentY * profile.verticalMomentumFactor + profile.initialVerticalImpulse
        },
        tuning
    )

    fun warlockSustainVelocity(
        currentY: Double,
        profile: MobilityProfile,
        tuning: PhysicsTuning
    ): Double = clampVertical(
        if (currentY < 0.0) {
            val softened = currentY * (1.0 - profile.sustainVerticalCorrection.coerceIn(0.0, 1.0))
            max(softened, -profile.fallingCushion.coerceAtLeast(0.01))
        }
        else currentY,
        tuning
    )

    fun clampVertical(value: Double, tuning: PhysicsTuning): Double {
        if (!value.isFinite()) return 0.0
        return value.coerceIn(-max(0.0, tuning.maxDownwardVelocity), max(0.0, tuning.maxUpwardVelocity))
    }

    private fun cushionedFallingVelocity(
        currentY: Double,
        momentumFactor: Double,
        fallingCushion: Double,
        recoveryFraction: Double,
        ceiling: Double
    ): Double {
        if (currentY >= 0.0) return min(ceiling, currentY)
        val factor = max(1.0, momentumFactor)
        val factorRecovery = currentY / factor - currentY
        val proportionalLimit = -currentY * recoveryFraction
        val recovery = min(fallingCushion, min(factorRecovery, proportionalLimit))
        return min(ceiling, currentY + max(0.0, recovery))
    }
}
