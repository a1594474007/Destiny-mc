package group.jumpenchants.equipment

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

fun interface EquipmentSource {
    fun equippedStacks(player: Player): Sequence<ItemStack>
}

