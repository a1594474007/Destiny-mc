package group.jumpenchants.state

import group.jumpenchants.movement.MobilityMode
import group.jumpenchants.movement.JobType
import net.minecraft.world.phys.Vec3

class JumpState {
    var mode: MobilityMode? = null
    var airborne = false
    var airborneTicks = 0
    var previousGrounded = true
    var extraJumpsUsed = 0
    var jumpDown = false
    var forwardInput = 0.0
    var strafeInput = 0.0
    var firstJumpApplied = false
    var firstJumpHoldTicks = 0
    var abilityInitialized = false
    var abilityActive = false
    var specialActivationUsed = false
    var capturedViewYaw = 0.0f
    var capturedViewPitch = 0.0f
    var budgetRemaining = 0
    var startupTicksRemaining = 0
    var fallingActivation = false
    var burstGraceTicks = 0
    var capturedDirection: Vec3 = Vec3.ZERO
    var lastInputSequence = -1
    var nextClientSequence = 0
    var lastHeartbeatTick = Int.MIN_VALUE
    var packetWindowStartTick = 0
    var packetsInWindow = 0
    var nextEquipmentCheckTick = 0
    var lastServerPosition: Vec3? = null
    var estimatedHorizontalMotion: Vec3 = Vec3.ZERO
    var lastMotionSampleTick = Int.MIN_VALUE
    var warlockBlinkCooldownUntilTick = 0
    var hunterBlinkCooldownUntilTick = 0

    fun blinkCooldownUntil(jobType: JobType): Int = when (jobType) {
        JobType.WARLOCK -> warlockBlinkCooldownUntilTick
        JobType.HUNTER -> hunterBlinkCooldownUntilTick
        JobType.TITAN -> Int.MAX_VALUE
    }

    fun setBlinkCooldown(jobType: JobType, untilTick: Int) {
        when (jobType) {
            JobType.WARLOCK -> warlockBlinkCooldownUntilTick = untilTick
            JobType.HUNTER -> hunterBlinkCooldownUntilTick = untilTick
            JobType.TITAN -> Unit
        }
    }

    fun switchMode(newMode: MobilityMode?) {
        if (mode == newMode) return
        val sequence = lastInputSequence
        val clientSequence = nextClientSequence
        resetAirborne()
        mode = newMode
        lastInputSequence = sequence
        nextClientSequence = clientSequence
    }

    fun beginAirborne() {
        airborne = true
        airborneTicks = 0
        extraJumpsUsed = 0
        firstJumpHoldTicks = 0
        abilityInitialized = false
        abilityActive = false
        specialActivationUsed = false
        capturedViewYaw = 0.0f
        capturedViewPitch = 0.0f
        budgetRemaining = 0
        startupTicksRemaining = 0
        fallingActivation = false
        burstGraceTicks = 0
        capturedDirection = Vec3.ZERO
    }

    fun resetAirborne() {
        airborne = false
        airborneTicks = 0
        extraJumpsUsed = 0
        jumpDown = false
        forwardInput = 0.0
        strafeInput = 0.0
        firstJumpApplied = false
        firstJumpHoldTicks = 0
        abilityInitialized = false
        abilityActive = false
        specialActivationUsed = false
        capturedViewYaw = 0.0f
        capturedViewPitch = 0.0f
        budgetRemaining = 0
        startupTicksRemaining = 0
        fallingActivation = false
        burstGraceTicks = 0
        capturedDirection = Vec3.ZERO
    }
}
