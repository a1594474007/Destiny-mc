package group.jumpenchants.movement

data class MobilityProfile(
    val initialVerticalImpulse: Double,
    val verticalMomentumFactor: Double,
    val propulsionFactor: Double,
    val propulsionDecayPerTick: Double,
    val minimumHorizontalPropulsionFactor: Double,
    val initialHorizontalImpulse: Double,
    val sustainHorizontalAcceleration: Double,
    val sustainVerticalCorrection: Double,
    val budgetTicks: Int,
    val steering: Double,
    val maxHorizontalSpeed: Double,
    val minimumGlideSpeed: Double,
    val momentumRetention: Double,
    val fallingCushion: Double
)

object DefaultProfiles {
    val values: Map<MobilityMode, MobilityProfile> = mapOf(
        MobilityMode.TITAN_FOCUSED_LIFT to MobilityProfile(
            initialVerticalImpulse = 1.0,
            verticalMomentumFactor = 1.20,
            propulsionFactor = 1.0,
            propulsionDecayPerTick = 0.06,
            minimumHorizontalPropulsionFactor = 0.25,
            initialHorizontalImpulse = 0.22,
            sustainHorizontalAcceleration = 0.035,
            sustainVerticalCorrection = 0.90,
            budgetTicks = 48,
            steering = 0.75,
            maxHorizontalSpeed = 1.0,
            minimumGlideSpeed = 0.0,
            momentumRetention = 0.90,
            fallingCushion = 0.24
        ),
        MobilityMode.TITAN_HIGH_LIFT to MobilityProfile(
            initialVerticalImpulse = 1.2,
            verticalMomentumFactor = 1.55,
            propulsionFactor = 1.0,
            propulsionDecayPerTick = 0.07,
            minimumHorizontalPropulsionFactor = 0.20,
            initialHorizontalImpulse = 0.16,
            sustainHorizontalAcceleration = 0.030,
            sustainVerticalCorrection = 0.80,
            budgetTicks = 56,
            steering = 0.28,
            maxHorizontalSpeed = 0.9,
            minimumGlideSpeed = 0.0,
            momentumRetention = 0.95,
            fallingCushion = 0.32
        ),
        MobilityMode.TITAN_CATAPULT_LIFT to MobilityProfile(
            initialVerticalImpulse = 1.05,
            verticalMomentumFactor = 1.65,
            propulsionFactor = 1.0,
            propulsionDecayPerTick = 0.18,
            minimumHorizontalPropulsionFactor = 0.10,
            initialHorizontalImpulse = 0.26,
            sustainHorizontalAcceleration = 0.040,
            sustainVerticalCorrection = 0.80,
            budgetTicks = 32,
            steering = 0.45,
            maxHorizontalSpeed = 1.1,
            minimumGlideSpeed = 0.0,
            momentumRetention = 0.85,
            fallingCushion = 0.34
        ),
        MobilityMode.WARLOCK_BALANCED_GLIDE to MobilityProfile(
            initialVerticalImpulse = 0.04,
            verticalMomentumFactor = 1.10,
            propulsionFactor = 1.0,
            propulsionDecayPerTick = 0.0,
            minimumHorizontalPropulsionFactor = 0.0,
            initialHorizontalImpulse = 0.34,
            sustainHorizontalAcceleration = 0.0,
            sustainVerticalCorrection = 0.12,
            budgetTicks = 24,
            steering = 0.18,
            maxHorizontalSpeed = 1.05,
            minimumGlideSpeed = 0.20,
            momentumRetention = 0.98,
            fallingCushion = 0.22
        ),
        MobilityMode.WARLOCK_BURST_GLIDE to MobilityProfile(
            initialVerticalImpulse = 0.08,
            verticalMomentumFactor = 1.25,
            propulsionFactor = 1.0,
            propulsionDecayPerTick = 0.0,
            minimumHorizontalPropulsionFactor = 0.0,
            initialHorizontalImpulse = 0.55,
            sustainHorizontalAcceleration = 0.0,
            sustainVerticalCorrection = 0.08,
            budgetTicks = 18,
            steering = 0.08,
            maxHorizontalSpeed = 1.35,
            minimumGlideSpeed = 0.30,
            momentumRetention = 1.00,
            fallingCushion = 0.18
        ),
        MobilityMode.WARLOCK_FOCUSED_GLIDE to MobilityProfile(
            initialVerticalImpulse = 0.02,
            verticalMomentumFactor = 1.05,
            propulsionFactor = 1.0,
            propulsionDecayPerTick = 0.0,
            minimumHorizontalPropulsionFactor = 0.0,
            initialHorizontalImpulse = 0.28,
            sustainHorizontalAcceleration = 0.0,
            sustainVerticalCorrection = 0.16,
            budgetTicks = 28,
            steering = 0.36,
            maxHorizontalSpeed = 1.00,
            minimumGlideSpeed = 0.16,
            momentumRetention = 0.97,
            fallingCushion = 0.25
        ),
        MobilityMode.HUNTER_STRAFE_JUMP to hunterProfile(
            initialVerticalImpulse = 0.42,
            steering = 0.95,
            momentumRetention = 1.0
        ),
        MobilityMode.HUNTER_HIGH_JUMP to hunterProfile(
            initialVerticalImpulse = 0.62,
            steering = 0.18,
            momentumRetention = 0.75
        ),
        MobilityMode.HUNTER_TRIPLE_JUMP to hunterProfile(
            initialVerticalImpulse = 0.38,
            steering = 0.55,
            momentumRetention = 0.90
        ),
        MobilityMode.WARLOCK_BLINK to inactiveProfile(),
        MobilityMode.HUNTER_BLINK to inactiveProfile()
    )

    private fun hunterProfile(
        initialVerticalImpulse: Double,
        steering: Double,
        momentumRetention: Double
    ) = MobilityProfile(
        initialVerticalImpulse = initialVerticalImpulse,
        verticalMomentumFactor = 1.0,
        propulsionFactor = 1.0,
        propulsionDecayPerTick = 0.0,
        minimumHorizontalPropulsionFactor = 0.0,
        initialHorizontalImpulse = 0.0,
        sustainHorizontalAcceleration = 0.0,
        sustainVerticalCorrection = 1.0,
        budgetTicks = 0,
        steering = steering,
        maxHorizontalSpeed = 1.0,
        minimumGlideSpeed = 0.0,
        momentumRetention = momentumRetention,
        fallingCushion = 0.0
    )

    private fun inactiveProfile() = MobilityProfile(
        initialVerticalImpulse = 0.0,
        verticalMomentumFactor = 1.0,
        propulsionFactor = 1.0,
        propulsionDecayPerTick = 0.0,
        minimumHorizontalPropulsionFactor = 0.0,
        initialHorizontalImpulse = 0.0,
        sustainHorizontalAcceleration = 0.0,
        sustainVerticalCorrection = 1.0,
        budgetTicks = 0,
        steering = 0.0,
        maxHorizontalSpeed = 0.0,
        minimumGlideSpeed = 0.0,
        momentumRetention = 0.0,
        fallingCushion = 0.0
    )
}
