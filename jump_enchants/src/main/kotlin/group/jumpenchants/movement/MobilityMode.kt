package group.jumpenchants.movement

enum class MobilityMode(
    val registryName: String,
    val jobType: JobType,
    val priority: Int,
    val maxExtraJumps: Int = 0,
    val isBlink: Boolean = false
) {
    HUNTER_BLINK("hunter_blink", JobType.HUNTER, 0, maxExtraJumps = 1, isBlink = true),
    WARLOCK_BLINK("warlock_blink", JobType.WARLOCK, 1, maxExtraJumps = 1, isBlink = true),
    HUNTER_TRIPLE_JUMP("hunter_triple_jump", JobType.HUNTER, 2, maxExtraJumps = 2),
    HUNTER_HIGH_JUMP("hunter_high_jump", JobType.HUNTER, 3, maxExtraJumps = 1),
    HUNTER_STRAFE_JUMP("hunter_strafe_jump", JobType.HUNTER, 4, maxExtraJumps = 1),
    WARLOCK_FOCUSED_GLIDE("warlock_focused_glide", JobType.WARLOCK, 5),
    WARLOCK_BURST_GLIDE("warlock_burst_glide", JobType.WARLOCK, 6),
    WARLOCK_BALANCED_GLIDE("warlock_balanced_glide", JobType.WARLOCK, 7),
    TITAN_CATAPULT_LIFT("titan_catapult_lift", JobType.TITAN, 8),
    TITAN_HIGH_LIFT("titan_high_lift", JobType.TITAN, 9),
    TITAN_FOCUSED_LIFT("titan_focused_lift", JobType.TITAN, 10);

    companion object {
        val PRIORITY_ORDER: List<MobilityMode> = entries.sortedBy(MobilityMode::priority)

        fun fromNetworkId(id: Int): MobilityMode? = entries.getOrNull(id)
    }
}
