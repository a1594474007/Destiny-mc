package group.jumpenchants.config

import group.jumpenchants.movement.DefaultProfiles
import group.jumpenchants.movement.JobType
import group.jumpenchants.movement.MobilityMode
import group.jumpenchants.movement.MobilityProfile
import group.jumpenchants.movement.PhysicsTuning
import net.minecraftforge.common.ForgeConfigSpec
import java.util.EnumMap

object JumpConfig {
    private val builder = ForgeConfigSpec.Builder()

    val agilitySoftCap: ForgeConfigSpec.DoubleValue
    val agilityImpulsePerPoint: ForgeConfigSpec.DoubleValue
    val hunterMaxHoldTicks: ForgeConfigSpec.IntValue
    val hunterHoldImpulsePerTick: ForgeConfigSpec.DoubleValue
    val inputPacketsPerSecond: ForgeConfigSpec.IntValue
    val packetRateWindowTicks: ForgeConfigSpec.IntValue
    val inputHeartbeatTicks: ForgeConfigSpec.IntValue
    val equipmentScanIntervalTicks: ForgeConfigSpec.IntValue
    val landingGraceTicks: ForgeConfigSpec.IntValue
    val debugHudEnabled: ForgeConfigSpec.BooleanValue
    val titanActivationCostTicks: ForgeConfigSpec.IntValue
    val warlockActivationCostTicks: ForgeConfigSpec.IntValue
    val maxUpwardAbilityVelocity: ForgeConfigSpec.DoubleValue
    val maxDownwardAbilityVelocity: ForgeConfigSpec.DoubleValue
    val maxSampledHorizontalSpeed: ForgeConfigSpec.DoubleValue
    val titanFallingVelocityFloor: ForgeConfigSpec.DoubleValue
    val titanFallingRecoveryFraction: ForgeConfigSpec.DoubleValue
    val titanFallDistanceReductionMultiplier: ForgeConfigSpec.DoubleValue
    val titanActivationWindowTicks: ForgeConfigSpec.IntValue
    val titanLockViewDuringAbility: ForgeConfigSpec.BooleanValue
    val titanViewLockYawToleranceDegrees: ForgeConfigSpec.DoubleValue
    val titanViewLockPitchToleranceDegrees: ForgeConfigSpec.DoubleValue
    val titanTurnAngularVelocityDegreesPerTick: ForgeConfigSpec.DoubleValue
    val titanHoverDescentSpeed: ForgeConfigSpec.DoubleValue
    val titanLookVerticalSpeed: ForgeConfigSpec.DoubleValue
    val titanMaxVerticalCorrection: ForgeConfigSpec.DoubleValue
    val titanBurstGraceTicks: ForgeConfigSpec.IntValue
    val titanBurstGlideSoftening: ForgeConfigSpec.DoubleValue
    val warlockFallingRecoveryFraction: ForgeConfigSpec.DoubleValue
    val warlockLookWeight: ForgeConfigSpec.DoubleValue
    val warlockInputWeight: ForgeConfigSpec.DoubleValue
    val warlockInFlightSteeringMultiplier: ForgeConfigSpec.DoubleValue
    val warlockLockViewDuringAbility: ForgeConfigSpec.BooleanValue
    val warlockViewLockYawToleranceDegrees: ForgeConfigSpec.DoubleValue
    val warlockViewLockPitchToleranceDegrees: ForgeConfigSpec.DoubleValue

    private val profileConfigs = EnumMap<MobilityMode, ProfileConfig>(MobilityMode::class.java)
    private val blinkConfigs = EnumMap<JobType, BlinkProfileConfig>(JobType::class.java)

    val SPEC: ForgeConfigSpec

    init {
        builder.push("general")
        inputPacketsPerSecond = builder
            .comment("玩家每秒最多允许发送的能力输入数据包数量")
            .defineInRange("inputPacketsPerSecond", 30, 5, 100)
        packetRateWindowTicks = builder
            .comment("服务端统计输入数据包速率的时间窗口 (tick)")
            .defineInRange("packetRateWindowTicks", 20, 1, 200)
        inputHeartbeatTicks = builder
            .comment("按住跳跃键时输入心跳数据包的发送间隔 (tick)")
            .defineInRange("inputHeartbeatTicks", 4, 1, 40)
        equipmentScanIntervalTicks = builder
            .comment("扫描护甲和饰品附魔的间隔 (tick)")
            .defineInRange("equipmentScanIntervalTicks", 5, 1, 200)
        landingGraceTicks = builder
            .comment("落地重置能力状态前所需的最短滞空时间 (tick)")
            .defineInRange("landingGraceTicks", 2, 0, 20)
        debugHudEnabled = builder
            .comment("是否在屏幕右下角显示能力调试信息")
            .define("debugHudEnabled", false)
        maxUpwardAbilityVelocity = builder
            .comment("能力产生的最高向上速度")
            .defineInRange("maxUpwardAbilityVelocity", 3.0, 0.0, 20.0)
        maxDownwardAbilityVelocity = builder
            .comment("能力处理后的最大下落速度")
            .defineInRange("maxDownwardAbilityVelocity", 4.0, 0.0, 20.0)
        maxSampledHorizontalSpeed = builder
            .comment("服务端估算水平速度时采用的上限")
            .defineInRange("maxSampledHorizontalSpeed", 4.0, 0.0, 20.0)
        builder.pop()

        builder.push("profile")

        builder.push("titan")
        builder.push("characteristics")
        titanActivationCostTicks = builder
            .comment("每次喷气消耗的燃料 (tick)")
            .defineInRange("activationCostTicks", 8, 1, 60)
        titanFallingVelocityFloor = builder.comment("下降喷射时允许达到的最高 Y 速度, 取值不得大于 0")
            .defineInRange("fallingVelocityFloor", -0.02, -2.0, 0.0)
        titanFallingRecoveryFraction = builder.comment("下降启动时最多消除的下坠速度比例")
            .defineInRange("fallingRecoveryFraction", 0.75, 0.0, 1.0)
        titanFallDistanceReductionMultiplier = builder.comment("下降缓冲量换算为摔落距离减免的倍率")
            .defineInRange("fallDistanceReductionMultiplier", 4.0, 0.0, 100.0)
        titanActivationWindowTicks = builder.comment("原版第一跳后允许启动跃升的时间窗口 (tick)")
            .defineInRange("activationWindowTicks", 8, 0, 200)
        titanLockViewDuringAbility = builder.comment("喷射期间是否限制视角")
            .define("lockViewDuringAbility", false)
        titanViewLockYawToleranceDegrees = builder.comment("喷射期间允许偏离初始视角的最大水平角度")
            .defineInRange("viewLockYawToleranceDegrees", 20.0, 0.0, 180.0)
        titanViewLockPitchToleranceDegrees = builder.comment("喷射期间允许偏离初始视角的最大俯仰角度")
            .defineInRange("viewLockPitchToleranceDegrees", 45.0, 0.0, 180.0)
        titanTurnAngularVelocityDegreesPerTick = builder
            .comment("喷射方向每 tick 允许转动的最大角度")
            .defineInRange("turnAngularVelocityDegreesPerTick", 45.0, 0.0, 180.0)
        titanHoverDescentSpeed = builder
            .comment("平视喷射时的目标下降速度")
            .defineInRange("hoverDescentSpeed", 0.12, 0.0, 1.0)
        titanLookVerticalSpeed = builder
            .comment("准星俯仰对目标纵向速度的影响强度")
            .defineInRange("lookVerticalSpeed", 0.70, 0.0, 3.0)
        titanMaxVerticalCorrection = builder
            .comment("每 tick 允许修正的最大纵向速度")
            .defineInRange("maxVerticalCorrection", 0.08, 0.0, 1.0)
        titanBurstGraceTicks = builder
            .comment("喷气后滞空缓落的持续时长 (tick)")
            .defineInRange("burstGraceTicks", 20, 0, 100)
        titanBurstGlideSoftening = builder
            .comment("喷气后缓落时削减下落速度的比例 (0=无缓落, 0.95=接近悬停)")
            .defineInRange("burstGlideSoftening", 0.6, 0.0, 0.95)
        titanBurstDecayMultiplier = builder
            .comment("第一次喷气后，后续每次喷气的冲量倍率 (0.5=减半)")
            .defineInRange("burstDecayMultiplier", 0.5, 0.1, 1.0)
        builder.pop()
        builder.push("abilities")
        registerAbilityProfiles(JobType.TITAN)
        builder.pop()
        builder.pop()

        builder.push("warlock")
        builder.push("characteristics")
        warlockActivationCostTicks = builder
            .comment("启动滑翔时扣除的滑翔时间 (tick)")
            .defineInRange("activationCostTicks", 2, 0, 20)
        warlockFallingRecoveryFraction = builder.comment("下降启动时最多消除的下坠速度比例")
            .defineInRange("fallingRecoveryFraction", 0.65, 0.0, 1.0)
        warlockLookWeight = builder.comment("准星方向对初始滑翔方向的影响权重")
            .defineInRange("lookWeight", 0.65, 0.0, 8.0)
        warlockInputWeight = builder.comment("移动输入对初始滑翔方向的影响权重")
            .defineInRange("inputWeight", 1.0, 0.0, 8.0)
        warlockInFlightSteeringMultiplier = builder
            .comment("滑翔中的方向修正倍率, 数值越小越难转向")
            .defineInRange("inFlightSteeringMultiplier", 0.10, 0.0, 1.0)
        warlockLockViewDuringAbility = builder.comment("滑翔期间是否限制视角")
            .define("lockViewDuringAbility", false)
        warlockViewLockYawToleranceDegrees = builder.comment("滑翔期间允许偏离初始视角的最大水平角度")
            .defineInRange("viewLockYawToleranceDegrees", 20.0, 0.0, 180.0)
        warlockViewLockPitchToleranceDegrees = builder.comment("滑翔期间允许偏离初始视角的最大俯仰角度")
            .defineInRange("viewLockPitchToleranceDegrees", 45.0, 0.0, 180.0)
        builder.pop()
        builder.push("abilities")
        registerAbilityProfiles(JobType.WARLOCK)
        blinkConfigs[JobType.WARLOCK] = BlinkProfileConfig(builder, "blink")
        builder.pop()
        builder.pop()

        builder.push("hunter")
        builder.push("characteristics")
        agilitySoftCap = builder.comment("猎人第一跳可计入加成的敏捷上限")
            .defineInRange("agilitySoftCap", 50.0, 0.0, 1024.0)
        agilityImpulsePerPoint = builder.comment("每点敏捷为第一跳增加的向上冲量")
            .defineInRange("agilityImpulsePerPoint", 0.004, 0.0, 0.25)
        hunterMaxHoldTicks = builder.comment("第一跳长按跳跃键可获得加速的时间 (tick)")
            .defineInRange("maxFirstJumpHoldTicks", 6, 0, 40)
        hunterHoldImpulsePerTick = builder
            .comment("第一跳长按期间每 tick 增加的向上冲量, 默认值可越过 1.5 格高台")
            .defineInRange("firstJumpHoldAccelerationPerTick", 0.020, 0.0, 0.25)
        builder.pop()
        builder.push("abilities")
        registerAbilityProfiles(JobType.HUNTER)
        blinkConfigs[JobType.HUNTER] = BlinkProfileConfig(builder, "blink")
        builder.pop()
        builder.pop()

        builder.pop()

        SPEC = builder.build()
    }

    fun profile(mode: MobilityMode): MobilityProfile = profileConfigs.getValue(mode).value()

    fun blink(mode: MobilityMode): BlinkSettings = blinkConfigs.getValue(mode.jobType).value()

    fun activationCostTicks(mode: MobilityMode): Int = when (mode.jobType) {
        JobType.TITAN -> titanActivationCostTicks.get()
        JobType.WARLOCK -> warlockActivationCostTicks.get()
        JobType.HUNTER -> 0
    }

    fun physicsTuning() = PhysicsTuning(
        maxUpwardAbilityVelocity.get(),
        maxDownwardAbilityVelocity.get(),
        titanFallingVelocityFloor.get(),
        titanFallingRecoveryFraction.get(),
        warlockFallingRecoveryFraction.get()
    )

    private fun registerAbilityProfiles(jobType: JobType) {
        for (mode in MobilityMode.entries) {
            if (mode.jobType != jobType || mode.isBlink) continue
            val name = mode.registryName.removePrefix("${jobType.name.lowercase()}_")
            profileConfigs[mode] = ProfileConfig(builder, name, mode, DefaultProfiles.values.getValue(mode))
        }
    }

    private class ProfileConfig(
        builder: ForgeConfigSpec.Builder,
        name: String,
        mode: MobilityMode,
        private val defaults: MobilityProfile
    ) {
        private val initialVerticalImpulse: ForgeConfigSpec.DoubleValue
        private val verticalMomentumFactor: ForgeConfigSpec.DoubleValue
        private val propulsionFactor: ForgeConfigSpec.DoubleValue?
        private val propulsionDecayPerTick: ForgeConfigSpec.DoubleValue?
        private val minimumHorizontalPropulsionFactor: ForgeConfigSpec.DoubleValue?
        private val initialHorizontalImpulse: ForgeConfigSpec.DoubleValue
        private val sustainHorizontalAcceleration: ForgeConfigSpec.DoubleValue
        private val sustainVerticalCorrection: ForgeConfigSpec.DoubleValue
        private val budgetTicks: ForgeConfigSpec.IntValue
        private val steering: ForgeConfigSpec.DoubleValue
        private val maxHorizontalSpeed: ForgeConfigSpec.DoubleValue
        private val minimumGlideSpeed: ForgeConfigSpec.DoubleValue
        private val momentumRetention: ForgeConfigSpec.DoubleValue
        private val fallingCushion: ForgeConfigSpec.DoubleValue

        init {
            builder.push(name)
            initialVerticalImpulse = builder
                .comment("启动阶段增加的 Y 速度")
                .defineInRange("initialVerticalImpulse", defaults.initialVerticalImpulse, 0.0, 4.0)
            verticalMomentumFactor = builder
                .comment("上升时放大继承速度, 下降时缩小下坠速度")
                .defineInRange("verticalMomentumFactor", defaults.verticalMomentumFactor, 1.0, 8.0)
            if (mode.jobType == JobType.TITAN) {
                propulsionFactor = builder
                    .comment("启动冲量和持续喷射共用的强度倍率")
                    .defineInRange("propulsionFactor", defaults.propulsionFactor, 0.0, 8.0)
                propulsionDecayPerTick = builder
                    .comment("每个燃料 tick 损失的剩余推力比例, 设为 0 时推力不衰减")
                    .defineInRange("propulsionDecayPerTick", defaults.propulsionDecayPerTick, 0.0, 1.0)
                minimumHorizontalPropulsionFactor = builder
                    .comment("纵向推力衰减后 XZ 推力至少保留的比例")
                    .defineInRange(
                        "minimumHorizontalPropulsionFactor",
                        defaults.minimumHorizontalPropulsionFactor,
                        0.0,
                        1.0
                    )
            } else {
                propulsionFactor = null
                propulsionDecayPerTick = null
                minimumHorizontalPropulsionFactor = null
            }
            initialHorizontalImpulse = builder.comment(
                when (mode.jobType) {
                    JobType.TITAN -> "启动喷射时沿准星 XZ 方向增加的冲量"
                    JobType.WARLOCK -> "启动滑翔时沿滑翔方向增加的水平冲量"
                    JobType.HUNTER -> "额外跳跃时沿移动输入方向增加的水平冲量"
                }
            ).defineInRange("initialHorizontalImpulse", defaults.initialHorizontalImpulse, 0.0, 4.0)
            sustainHorizontalAcceleration = builder
                .comment("持续阶段每 tick 增加的 XZ 速度")
                .defineInRange("sustainHorizontalAcceleration", defaults.sustainHorizontalAcceleration, 0.0, 1.0)
            sustainVerticalCorrection = builder
                .comment("持续阶段纵向补偿强度")
                .defineInRange("sustainVerticalCorrection", defaults.sustainVerticalCorrection, 0.0, 1.0)
            val budgetKey = when (mode.jobType) {
                JobType.TITAN -> "fuelTicks"
                JobType.WARLOCK -> "glideTicks"
                JobType.HUNTER -> "budgetTicks"
            }
            budgetTicks = builder.comment("技能的燃料或持续时间 (tick)")
                .defineInRange(budgetKey, defaults.budgetTicks, 0, 200)
            steering = builder.comment("方向控制强度")
                .defineInRange("steering", defaults.steering, 0.0, 1.0)
            maxHorizontalSpeed = builder.comment("最高水平速度")
                .defineInRange("maxHorizontalSpeed", defaults.maxHorizontalSpeed, 0.0, 4.0)
            minimumGlideSpeed = builder
                .comment(if (mode.jobType == JobType.WARLOCK) "滑翔时沿已捕获航向维持的最低速度" else "该技能不生成最低水平速度")
                .defineInRange("minimumGlideSpeed", defaults.minimumGlideSpeed, 0.0, 4.0)
            momentumRetention = builder.comment("计算和维持移动方向时保留的水平动量比例")
                .defineInRange("momentumRetention", defaults.momentumRetention, 0.0, 2.0)
            fallingCushion = builder.comment("下降中启动技能时最多消除的下坠速度")
                .defineInRange("fallingCushion", defaults.fallingCushion, 0.0, 4.0)
            builder.pop()
        }

        fun value() = MobilityProfile(
            initialVerticalImpulse.get(),
            verticalMomentumFactor.get(),
            propulsionFactor?.get() ?: defaults.propulsionFactor,
            propulsionDecayPerTick?.get() ?: defaults.propulsionDecayPerTick,
            minimumHorizontalPropulsionFactor?.get() ?: defaults.minimumHorizontalPropulsionFactor,
            initialHorizontalImpulse.get(),
            sustainHorizontalAcceleration.get(),
            sustainVerticalCorrection.get(),
            budgetTicks.get(),
            steering.get(),
            maxHorizontalSpeed.get(),
            minimumGlideSpeed.get(),
            momentumRetention.get(),
            fallingCushion.get()
        )
    }

    private class BlinkProfileConfig(builder: ForgeConfigSpec.Builder, name: String) {
        private val distance: ForgeConfigSpec.DoubleValue
        private val verticalVelocity: ForgeConfigSpec.DoubleValue
        private val failureConsumesUse: ForgeConfigSpec.BooleanValue
        private val activationDelayTicks: ForgeConfigSpec.IntValue
        private val minimumGroundClearance: ForgeConfigSpec.DoubleValue
        private val cooldownTicks: ForgeConfigSpec.IntValue
        private val collisionSearchStep: ForgeConfigSpec.DoubleValue
        private val wallMargin: ForgeConfigSpec.DoubleValue

        init {
            builder.push(name)
            distance = builder.comment("闪现距离")
                .defineInRange("distance", 6.0, 0.5, 32.0)
            verticalVelocity = builder.comment("闪现成功后的纵向速度")
                .defineInRange("verticalVelocity", 0.0, -2.0, 2.0)
            failureConsumesUse = builder.comment("找不到安全落点时是否消耗本次闪现")
                .define("failureConsumesUse", false)
            activationDelayTicks = builder.comment("离地后自动允许闪现的等待时间 (tick)")
                .defineInRange("activationDelayTicks", 4, 0, 200)
            minimumGroundClearance = builder.comment("提前允许闪现所需的最低离地高度")
                .defineInRange("minimumGroundClearance", 0.5, 0.0, 16.0)
            cooldownTicks = builder.comment("闪现冷却时间 (tick), 设为 0 时不冷却")
                .defineInRange("cooldownTicks", 0, 0, 72000)
            collisionSearchStep = builder.comment("沿闪现路径搜索安全落点时的步长")
                .defineInRange("collisionSearchStep", 0.25, 0.05, 2.0)
            wallMargin = builder.comment("闪现终点与障碍物之间保留的距离")
                .defineInRange("wallMargin", 0.30, 0.0, 2.0)
            builder.pop()
        }

        fun value() = BlinkSettings(
            distance.get(),
            verticalVelocity.get(),
            failureConsumesUse.get(),
            activationDelayTicks.get(),
            minimumGroundClearance.get(),
            cooldownTicks.get(),
            collisionSearchStep.get(),
            wallMargin.get()
        )
    }
}

data class BlinkSettings(
    val distance: Double,
    val verticalVelocity: Double,
    val failureConsumesUse: Boolean,
    val activationDelayTicks: Int,
    val minimumGroundClearance: Double,
    val cooldownTicks: Int,
    val collisionSearchStep: Double,
    val wallMargin: Double
)
