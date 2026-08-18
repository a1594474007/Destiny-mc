package group.jumpenchants.client

import group.jumpenchants.config.JumpConfig
import group.jumpenchants.movement.JobType
import group.jumpenchants.registry.ModAttributes
import group.jumpenchants.state.JumpState
import group.jumpenchants.state.JumpStates
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import net.minecraftforge.client.event.RenderGuiEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.Locale
import kotlin.math.sqrt

object DebugHud {
    private const val LINE_HEIGHT = 10
    private const val MARGIN = 6
    private const val TEXT_COLOR = 0xFFE6E6E6.toInt()
    private const val TITLE_COLOR = 0xFF7DFFB2.toInt()

    @SubscribeEvent
    fun onRenderGui(event: RenderGuiEvent.Post) {
        if (!JumpConfig.debugHudEnabled.get()) return
        val minecraft = Minecraft.getInstance()
        if (minecraft.options.hideGui) return
        val player = minecraft.player ?: return
        val state = JumpStates.get(player)
        val mode = state.mode ?: return
        val motion = player.deltaMovement
        val look = player.lookAngle
        val lines = mutableListOf<String>()

        lines += "Jump Enchants Debug"
        val abilityName = Component.translatable("enchantment.jump_enchants.${mode.registryName}").string
        lines += "能力 $abilityName (${mode.registryName})  职业 ${mode.jobType.name}  阶段 ${phase(state)}"
        lines += "输入 前进=${f(state.forwardInput)} 横移=${f(state.strafeInput)} 跳跃=${state.jumpDown}"
        lines += "速度 X=${f(motion.x)} Y=${f(motion.y)} Z=${f(motion.z)}"
        lines += "速度 水平=${f(horizontalLength(motion))} 总计=${f(motion.length())}"
        lines += "镜头 yaw=${f(player.yRot.toDouble())} pitch=${f(player.xRot.toDouble())}"
        if (mode.jobType != JobType.HUNTER && !mode.isBlink) {
            val viewLock = if (mode.jobType == JobType.TITAN) {
                Triple(
                    JumpConfig.titanLockViewDuringAbility.get(),
                    JumpConfig.titanViewLockYawToleranceDegrees.get(),
                    JumpConfig.titanViewLockPitchToleranceDegrees.get()
                )
            } else {
                Triple(
                    JumpConfig.warlockLockViewDuringAbility.get(),
                    JumpConfig.warlockViewLockYawToleranceDegrees.get(),
                    JumpConfig.warlockViewLockPitchToleranceDegrees.get()
                )
            }
            lines += "锁定 ${viewLock.first} " +
                "中心=${f(state.capturedViewYaw.toDouble())}/${f(state.capturedViewPitch.toDouble())} " +
                "宽容=${f(viewLock.second)}/${f(viewLock.third)}"
        }
        lines += "面向 X=${f(look.x)} Y=${f(look.y)} Z=${f(look.z)}"
        lines += "捕获 X=${f(state.capturedDirection.x)} " +
            "Y=${f(state.capturedDirection.y)} Z=${f(state.capturedDirection.z)}"
        lines += "状态 离地=${state.airborneTicks} 激活=${state.abilityActive} " +
            "已用=${state.specialActivationUsed} 下落启动=${state.fallingActivation}"
        lines += "计数 额外跳=${state.extraJumpsUsed}/${mode.maxExtraJumps} " +
            "预算=${budget(state)} 启动=${state.startupTicksRemaining}"

        if (mode.jobType == JobType.HUNTER) {
            val agility = player.getAttributeValue(ModAttributes.AGILITY.get())
            lines += "猎人 敏捷=${f(agility)} 长按=${state.firstJumpHoldTicks}/${JumpConfig.hunterMaxHoldTicks.get()}"
        }

        if (mode.isBlink) {
            val blink = JumpConfig.blink(mode)
            lines += "闪现 距离=${f(blink.distance)} Y=${f(blink.verticalVelocity)} 冷却=${blink.cooldownTicks}"
            lines += "闪现 解锁=${blink.activationDelayTicks}tick 或 ${f(blink.minimumGroundClearance)}格"
        } else {
            val profile = JumpConfig.profile(mode)
            lines += "初始 Y=${f(profile.initialVerticalImpulse)} " +
                "XZ=${f(profile.initialHorizontalImpulse)} 动量=${f(profile.verticalMomentumFactor)}"
            lines += "持续 XZ=${f(profile.sustainHorizontalAcceleration)} Y修正=${f(profile.sustainVerticalCorrection)}"
            lines += "喷气 倍率=${f(profile.propulsionFactor)} " +
                "衰减=${f(profile.propulsionDecayPerTick)} " +
                "尾推=${f(profile.minimumHorizontalPropulsionFactor)}"
            lines += "水平 上限=${f(profile.maxHorizontalSpeed)} " +
                "下限=${f(profile.minimumGlideSpeed)} 保留=${f(profile.momentumRetention)}"
            lines += "控制 转向=${f(profile.steering)} 缓冲=${f(profile.fallingCushion)} 配置预算=${profile.budgetTicks}"
        }

        val font = minecraft.font
        val width = lines.maxOf(font::width)
        val height = lines.size * LINE_HEIGHT
        val left = (event.window.guiScaledWidth - width - MARGIN).coerceAtLeast(MARGIN)
        val top = (event.window.guiScaledHeight - height - MARGIN).coerceAtLeast(MARGIN)
        val graphics = event.guiGraphics
        graphics.fill(
            left - 3,
            top - 3,
            event.window.guiScaledWidth - 3,
            top + height + 3,
            0xA0000000.toInt()
        )
        lines.forEachIndexed { index, line ->
            val x = event.window.guiScaledWidth - MARGIN - font.width(line)
            graphics.drawString(
                font,
                line,
                x.coerceAtLeast(MARGIN),
                top + index * LINE_HEIGHT,
                if (index == 0) TITLE_COLOR else TEXT_COLOR,
                true
            )
        }
    }

    private fun phase(state: JumpState): String = when {
        !state.airborne -> "GROUND"
        state.abilityActive && state.startupTicksRemaining > 0 -> "STARTUP"
        state.abilityActive && state.mode?.jobType == JobType.WARLOCK -> "GLIDE"
        state.abilityActive -> "POWERED"
        state.specialActivationUsed -> "EXHAUSTED"
        else -> "READY"
    }

    private fun budget(state: JumpState): String =
        if (state.budgetRemaining == Int.MAX_VALUE) "无限" else state.budgetRemaining.toString()

    private fun horizontalLength(vector: Vec3): Double = sqrt(vector.x * vector.x + vector.z * vector.z)

    private fun f(value: Double): String = String.format(Locale.ROOT, "%.3f", value)
}
