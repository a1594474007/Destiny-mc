package group.jumpenchants.movement

import group.jumpenchants.config.JumpConfig
import group.jumpenchants.registry.ModAttributes
import group.jumpenchants.state.JumpState
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow

object MovementController {
    enum class ActionResult {
        ACCEPTED,
        REJECTED,
        PENDING_SERVER,
        CANCELLED
    }

    fun canUse(player: Player): Boolean =
        !(
            player.isSpectator ||
                player.abilities.flying ||
                player.isFallFlying ||
                player.isPassenger ||
                player.isSleeping ||
                player.isDeadOrDying ||
                player.onClimbable() ||
                player.isSwimming ||
                player.isInWaterOrBubble
        )

    fun sampleServerHorizontalMotion(player: ServerPlayer, state: JumpState) {
        if (state.lastMotionSampleTick == player.tickCount) return
        val position = player.position()
        val previous = state.lastServerPosition
        if (previous != null) {
            val displacement = position.subtract(previous)
            state.estimatedHorizontalMotion = MovementMath.clampHorizontal(
                Vec3(displacement.x, 0.0, displacement.z),
                JumpConfig.maxSampledHorizontalSpeed.get()
            )
        }
        state.lastServerPosition = position
        state.lastMotionSampleTick = player.tickCount
    }

    fun beginFirstJump(
        player: Player,
        state: JumpState,
        mode: MobilityMode,
        resetForVanillaJump: Boolean = false
    ) {
        state.switchMode(mode)
        if (resetForVanillaJump || !state.airborne) state.beginAirborne()
        applyHunterFirstJump(player, state, mode)
    }

    fun applyHunterFirstJump(player: Player, state: JumpState, mode: MobilityMode) {
        if (state.firstJumpApplied || mode.jobType != JobType.HUNTER) return
        val agility = player.getAttributeValue(ModAttributes.AGILITY.get())
            .coerceIn(0.0, JumpConfig.agilitySoftCap.get())
        if (agility > 0.0) {
            val motion = authoritativeMotion(player, state)
            val nextY = MovementPhysics.hunterFirstJumpVelocity(
                motion.y,
                agility,
                JumpConfig.agilityImpulsePerPoint.get(),
                JumpConfig.physicsTuning()
            )
            setMotion(player, Vec3(motion.x, nextY, motion.z))
        }
        state.firstJumpApplied = true
    }

    fun release(state: JumpState) {
        state.jumpDown = false
    }

    fun activate(
        player: Player,
        state: JumpState,
        mode: MobilityMode,
        input: MovementInput
    ): ActionResult {
        if (!canUse(player) || !state.airborne) return ActionResult.REJECTED
        return when (mode.jobType) {
            JobType.TITAN -> if (state.specialActivationUsed) {
                state.abilityActive = false
                ActionResult.CANCELLED
            } else {
                activateTitan(player, state, mode)
            }
            JobType.WARLOCK -> if (mode.isBlink) {
                activateBlink(player, state, mode)
            } else if (state.specialActivationUsed) {
                state.abilityActive = false
                ActionResult.CANCELLED
            } else {
                activateWarlock(player, state, mode, input)
            }
            JobType.HUNTER -> if (mode.isBlink) {
                activateBlink(player, state, mode)
            } else {
                activateHunter(player, state, mode, input)
            }
        }
    }

    fun tick(player: Player, state: JumpState) {
        val mode = state.mode ?: return
        if (!state.airborne || !canUse(player)) {
            state.abilityActive = false
            return
        }

        state.airborneTicks++
        tickHunterFirstJumpHold(player, state, mode)

        if (!state.abilityActive || state.budgetRemaining <= 0) return
        when (mode.jobType) {
            JobType.TITAN -> tickTitan(player, state, mode)
            JobType.WARLOCK -> if (!mode.isBlink) tickWarlock(player, state, mode)
            JobType.HUNTER -> Unit
        }
    }

    private fun tickHunterFirstJumpHold(player: Player, state: JumpState, mode: MobilityMode) {
        if (
            mode.jobType != JobType.HUNTER ||
            !state.firstJumpApplied ||
            !state.jumpDown ||
            state.extraJumpsUsed > 0 ||
            state.firstJumpHoldTicks >= JumpConfig.hunterMaxHoldTicks.get() ||
            player.deltaMovement.y <= 0.0 ||
            player.verticalCollision
        ) return

        val motion = authoritativeMotion(player, state)
        val nextY = MovementPhysics.hunterHoldVelocity(
            motion.y,
            JumpConfig.hunterHoldImpulsePerTick.get(),
            JumpConfig.physicsTuning()
        )
        setMotion(player, Vec3(motion.x, nextY, motion.z))
        state.firstJumpHoldTicks++
    }

    private fun activateTitan(
        player: Player,
        state: JumpState,
        mode: MobilityMode
    ): ActionResult {
        val profile = JumpConfig.profile(mode)
        if (state.airborneTicks > JumpConfig.titanActivationWindowTicks.get()) return ActionResult.REJECTED
        if (!ensureBudget(state, profile)) return ActionResult.REJECTED

        state.abilityActive = true
        state.specialActivationUsed = true
        state.capturedViewYaw = player.yRot
        state.capturedViewPitch = player.xRot
        val motion = authoritativeMotion(player, state)
        state.fallingActivation = motion.y <= 0.0
        state.capturedDirection = player.lookAngle.normalize()
        consumeActivationCost(state)

        val newY = MovementPhysics.titanActivationVelocity(
            motion.y,
            profile,
            state.fallingActivation,
            JumpConfig.physicsTuning()
        )
        val thrustDirection = state.capturedDirection
        val directionalImpulse = MovementMath.horizontal(thrustDirection)
            .scale(profile.initialHorizontalImpulse * profile.propulsionFactor)
        var boosted = Vec3(motion.x, newY, motion.z).add(directionalImpulse)
        if (state.fallingActivation) {
            boosted = Vec3(
                boosted.x,
                minOf(JumpConfig.titanFallingVelocityFloor.get(), boosted.y),
                boosted.z
            )
        }
        boosted = MovementMath.clampHorizontal(
            boosted,
            profile.maxHorizontalSpeed * profile.propulsionFactor
        )
        setMotion(player, boosted)
        if (state.fallingActivation) {
            player.fallDistance = max(
                0.0f,
                player.fallDistance -
                    (profile.fallingCushion * JumpConfig.titanFallDistanceReductionMultiplier.get()).toFloat()
            )
        }
        return ActionResult.ACCEPTED
    }

    private fun tickTitan(player: Player, state: JumpState, mode: MobilityMode) {
        val profile = JumpConfig.profile(mode)
        val motion = authoritativeMotion(player, state)
        val targetDirection = player.lookAngle.normalize()
        val maxTurnRadians =
            JumpConfig.titanTurnAngularVelocityDegreesPerTick.get() *
                profile.steering * PI / 180.0
        state.capturedDirection = MovementMath.rotateTowards(
            state.capturedDirection,
            targetDirection,
            maxTurnRadians
        )
        val poweredTicks = (profile.budgetTicks - state.budgetRemaining).coerceAtLeast(0)
        val decay = (1.0 - profile.propulsionDecayPerTick)
            .coerceIn(0.0, 1.0)
            .pow(poweredTicks)
        val horizontalFactor = max(decay, profile.minimumHorizontalPropulsionFactor)
        val baseAcceleration = profile.sustainHorizontalAcceleration * profile.propulsionFactor
        val verticalCorrection = MovementPhysics.titanVerticalCorrection(
            motion.y,
            state.capturedDirection.y,
            state.fallingActivation,
            JumpConfig.titanHoverDescentSpeed.get(),
            JumpConfig.titanLookVerticalSpeed.get(),
            JumpConfig.titanMaxVerticalCorrection.get() * profile.sustainVerticalCorrection,
            JumpConfig.physicsTuning()
        )
        val thrust = Vec3(
            state.capturedDirection.x * baseAcceleration * horizontalFactor,
            verticalCorrection,
            state.capturedDirection.z * baseAcceleration * horizontalFactor
        )
        val next = MovementMath.clampHorizontal(
            motion.add(thrust),
            profile.maxHorizontalSpeed * profile.propulsionFactor
        )
        setMotion(player, next)
        consumeTick(state)
    }

    private fun activateWarlock(
        player: Player,
        state: JumpState,
        mode: MobilityMode,
        input: MovementInput
    ): ActionResult {
        val profile = JumpConfig.profile(mode)
        if (!ensureBudget(state, profile)) return ActionResult.REJECTED

        state.abilityActive = true
        state.specialActivationUsed = true
        state.capturedViewYaw = player.yRot
        state.capturedViewPitch = player.xRot
        val motion = authoritativeMotion(player, state)
        state.fallingActivation = motion.y <= 0.0
        state.capturedDirection = warlockDirection(player, state, input, profile)
        consumeActivationCost(state)
        state.budgetRemaining = Int.MAX_VALUE
        state.startupTicksRemaining = JumpConfig.warlockActivationCostTicks.get()
            .coerceAtMost(state.budgetRemaining)

        val nextY = MovementPhysics.warlockActivationVelocity(
            motion.y,
            profile,
            state.fallingActivation,
            JumpConfig.physicsTuning()
        )
        val horizontalImpulse = MovementMath.horizontal(state.capturedDirection)
            .scale(profile.initialHorizontalImpulse)
        val next = MovementMath.clampHorizontal(
            Vec3(motion.x + horizontalImpulse.x, nextY, motion.z + horizontalImpulse.z),
            profile.maxHorizontalSpeed
        )
        setMotion(player, next)
        return ActionResult.ACCEPTED
    }

    private fun tickWarlock(player: Player, state: JumpState, mode: MobilityMode) {
        val profile = JumpConfig.profile(mode)
        if (state.startupTicksRemaining > 0) {
            state.startupTicksRemaining--
            return
        }
        val liveDirection = MovementMath.inputDirection(
            player.yRot,
            MovementInput(state.forwardInput, state.strafeInput)
        )
        if (liveDirection != Vec3.ZERO) {
            val amount = profile.steering * JumpConfig.warlockInFlightSteeringMultiplier.get()
            val redirected = state.capturedDirection.lerp(liveDirection, amount)
            if (redirected.lengthSqr() > 1.0e-8) state.capturedDirection = redirected.normalize()
        }

        val motion = authoritativeMotion(player, state)
        val currentSpeed = MovementMath.horizontalLength(motion)
        val retainedSpeed = (currentSpeed * profile.momentumRetention)
            .coerceAtMost(profile.maxHorizontalSpeed)
        val desired = state.capturedDirection.scale(retainedSpeed)
        var next = MovementMath.steer(motion, desired, profile.steering)
        val nextY = MovementPhysics.warlockSustainVelocity(
            motion.y,
            profile,
            JumpConfig.physicsTuning()
        )
        next = MovementMath.clampHorizontal(Vec3(next.x, nextY, next.z), profile.maxHorizontalSpeed)
        if (
            currentSpeed > 1.0e-6 &&
            profile.minimumGlideSpeed > 0.0 &&
            state.capturedDirection.lengthSqr() > 1.0e-8
        ) {
            val glideSpeed = MovementMath.horizontalLength(next)
            if (glideSpeed in 1.0e-6..<profile.minimumGlideSpeed) {
                val direction = MovementMath.horizontal(state.capturedDirection).normalize()
                next = Vec3(
                    direction.x * profile.minimumGlideSpeed,
                    next.y,
                    direction.z * profile.minimumGlideSpeed
                )
            }
        }
        setMotion(player, next)
    }

    private fun activateHunter(
        player: Player,
        state: JumpState,
        mode: MobilityMode,
        input: MovementInput
    ): ActionResult {
        if (state.extraJumpsUsed >= mode.maxExtraJumps) return ActionResult.REJECTED
        val profile = JumpConfig.profile(mode)
        val direction = MovementMath.inputDirection(player.yRot, input)
        val motion = authoritativeMotion(player, state)
        val currentHorizontal = MovementMath.horizontal(motion)
        val currentSpeed = currentHorizontal.length()
        val inputStrength = (input.forward * input.forward + input.strafe * input.strafe)
            .toDouble()
            .coerceIn(0.0, 1.0)
        val redirected = if (direction.lengthSqr() < 1.0e-8 || currentSpeed < 1.0e-8) {
            currentHorizontal
        } else {
            val currentDirection = currentHorizontal.normalize()
            val inputWeight = (profile.steering.coerceIn(0.0, 1.0) * inputStrength)
            val targetDirection = currentDirection
                .scale(1.0 - inputWeight)
                .add(direction.scale(inputWeight))
                .let { if (it.lengthSqr() < 1.0e-8) direction else it.normalize() }
            targetDirection.scale(currentSpeed * profile.momentumRetention.coerceIn(0.0, 1.0))
        }
        val redirectedJump = Vec3(
            redirected.x,
            MovementPhysics.hunterExtraJumpVelocity(profile.initialVerticalImpulse, JumpConfig.physicsTuning()),
            redirected.z
        )
        val next = MovementMath.clampHorizontal(redirectedJump, profile.maxHorizontalSpeed)
        setMotion(player, next)
        player.fallDistance = 0.0f
        state.extraJumpsUsed++
        return ActionResult.ACCEPTED
    }

    private fun activateBlink(player: Player, state: JumpState, mode: MobilityMode): ActionResult {
        if (state.extraJumpsUsed >= 1) return ActionResult.REJECTED
        val settings = JumpConfig.blink(mode)
        val activationReady =
            state.airborneTicks >= settings.activationDelayTicks ||
                BlinkService.hasGroundClearance(player, settings)
        if (!activationReady || player.tickCount < state.blinkCooldownUntil(mode.jobType)) {
            return ActionResult.REJECTED
        }
        if (player.level().isClientSide) {
            state.extraJumpsUsed++
            return ActionResult.PENDING_SERVER
        }
        val succeeded = BlinkService.tryBlink(player as ServerPlayer, settings)
        if (succeeded) {
            state.extraJumpsUsed++
            state.setBlinkCooldown(mode.jobType, player.tickCount + settings.cooldownTicks)
        } else if (settings.failureConsumesUse) {
            state.extraJumpsUsed++
        }
        return if (succeeded) ActionResult.ACCEPTED else ActionResult.REJECTED
    }

    private fun ensureBudget(state: JumpState, profile: MobilityProfile): Boolean {
        if (!state.abilityInitialized) {
            state.abilityInitialized = true
            state.budgetRemaining = profile.budgetTicks
        }
        return state.budgetRemaining > 0
    }

    private fun consumeActivationCost(state: JumpState) {
        val mode = state.mode ?: return
        state.budgetRemaining = max(0, state.budgetRemaining - JumpConfig.activationCostTicks(mode))
        if (state.budgetRemaining == 0) state.abilityActive = false
    }

    private fun consumeTick(state: JumpState) {
        state.budgetRemaining = max(0, state.budgetRemaining - 1)
        if (state.budgetRemaining == 0) state.abilityActive = false
    }

    private fun warlockDirection(
        player: Player,
        state: JumpState,
        input: MovementInput,
        profile: MobilityProfile
    ): Vec3 {
        val motion = authoritativeMotion(player, state)
        val look = MovementMath.horizontal(player.lookAngle)
        val movementInput = MovementMath.inputDirection(player.yRot, input)
        val mixed = MovementMath.horizontal(motion).scale(profile.momentumRetention)
            .add(look.scale(JumpConfig.warlockLookWeight.get()))
            .add(movementInput.scale(JumpConfig.warlockInputWeight.get() + profile.steering))
        if (mixed.lengthSqr() >= 1.0e-8) return mixed.normalize()
        return horizontalAim(player, state, input)
    }

    private fun horizontalAim(player: Player, state: JumpState, input: MovementInput): Vec3 {
        val look = MovementMath.horizontal(player.lookAngle)
        if (look.lengthSqr() >= 1.0e-8) return look.normalize()
        val inputDirection = MovementMath.inputDirection(player.yRot, input)
        if (inputDirection != Vec3.ZERO) return inputDirection
        val momentum = MovementMath.horizontal(authoritativeMotion(player, state))
        return if (momentum.lengthSqr() >= 1.0e-8) momentum.normalize() else Vec3.ZERO
    }

    private fun authoritativeMotion(player: Player, state: JumpState): Vec3 {
        val motion = player.deltaMovement
        if (player.level().isClientSide || player !is ServerPlayer) return motion
        return Vec3(state.estimatedHorizontalMotion.x, motion.y, state.estimatedHorizontalMotion.z)
    }

    private fun setMotion(player: Player, motion: Vec3) {
        val sanitized = Vec3(
            if (motion.x.isFinite()) motion.x else 0.0,
            MovementPhysics.clampVertical(motion.y, JumpConfig.physicsTuning()),
            if (motion.z.isFinite()) motion.z else 0.0
        )
        player.deltaMovement = sanitized
        if (player.level().isClientSide) player.hasImpulse = true
    }
}
