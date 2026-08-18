package group.jumpenchants.movement

data class MovementInput(
    val forward: Double,
    val strafe: Double
) {
    fun clamped() = MovementInput(
        forward.coerceIn(-1.0, 1.0),
        strafe.coerceIn(-1.0, 1.0)
    )
}

