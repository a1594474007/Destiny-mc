package group.jumpenchants.movement

import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

object MovementMath {
    fun inputDirection(yawDegrees: Float, input: MovementInput): Vec3 {
        val clamped = input.clamped()
        val lengthSquared = clamped.forward * clamped.forward + clamped.strafe * clamped.strafe
        if (lengthSquared < 1.0e-8) return Vec3.ZERO

        val scale = 1.0 / sqrt(lengthSquared.coerceAtLeast(1.0))
        val forward = clamped.forward * scale
        val strafe = clamped.strafe * scale
        val yaw = yawDegrees * Mth.DEG_TO_RAD
        val sinYaw = sin(yaw.toDouble())
        val cosYaw = cos(yaw.toDouble())

        return Vec3(
            strafe * cosYaw - forward * sinYaw,
            0.0,
            forward * cosYaw + strafe * sinYaw
        )
    }

    fun horizontal(vector: Vec3): Vec3 = Vec3(vector.x, 0.0, vector.z)

    fun clampHorizontal(vector: Vec3, maxSpeed: Double): Vec3 {
        val horizontalLength = sqrt(vector.x * vector.x + vector.z * vector.z)
        if (horizontalLength <= maxSpeed || horizontalLength < 1.0e-8) return vector
        val scale = maxSpeed / horizontalLength
        return Vec3(vector.x * scale, vector.y, vector.z * scale)
    }

    fun steer(current: Vec3, desired: Vec3, amount: Double): Vec3 {
        val t = amount.coerceIn(0.0, 1.0)
        return Vec3(
            current.x + (desired.x - current.x) * t,
            current.y,
            current.z + (desired.z - current.z) * t
        )
    }

    fun horizontalLength(vector: Vec3): Double = sqrt(vector.x * vector.x + vector.z * vector.z)

    fun rotateTowardsHorizontal(current: Vec3, target: Vec3, maxRadians: Double): Vec3 {
        val flatCurrent = horizontal(current)
        val flatTarget = horizontal(target)
        if (flatTarget.lengthSqr() < 1.0e-8) return flatCurrent
        if (flatCurrent.lengthSqr() < 1.0e-8) return flatTarget.normalize()

        val currentAngle = atan2(flatCurrent.z, flatCurrent.x)
        val targetAngle = atan2(flatTarget.z, flatTarget.x)
        var difference = targetAngle - currentAngle
        while (difference > PI) difference -= PI * 2.0
        while (difference < -PI) difference += PI * 2.0
        val nextAngle = currentAngle + difference.coerceIn(-maxRadians, maxRadians)
        return Vec3(cos(nextAngle), 0.0, sin(nextAngle))
    }

    fun rotateTowards(current: Vec3, target: Vec3, maxRadians: Double): Vec3 {
        if (target.lengthSqr() < 1.0e-8) return current
        if (current.lengthSqr() < 1.0e-8) return target.normalize()
        val from = current.normalize()
        val to = target.normalize()
        val angle = acos(from.dot(to).coerceIn(-1.0, 1.0))
        if (angle <= maxRadians || angle < 1.0e-8) return to
        val blended = from.lerp(to, (maxRadians / angle).coerceIn(0.0, 1.0))
        return if (blended.lengthSqr() < 1.0e-8) to else blended.normalize()
    }
}
