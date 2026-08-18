package group.jumpenchants.registry

import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentCategory

class JumpEnchantment : Enchantment(
    Rarity.VERY_RARE,
    EnchantmentCategory.ARMOR,
    arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
) {
    override fun getMaxLevel(): Int = 1

    override fun isDiscoverable(): Boolean = false

    override fun isTradeable(): Boolean = false

    override fun isAllowedOnBooks(): Boolean = false

    override fun canEnchant(stack: ItemStack): Boolean = !stack.isEmpty

    override fun checkCompatibility(other: Enchantment): Boolean =
        other !is JumpEnchantment && super.checkCompatibility(other)
}

