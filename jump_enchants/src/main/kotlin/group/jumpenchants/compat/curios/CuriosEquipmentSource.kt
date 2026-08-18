package group.jumpenchants.compat.curios

import group.jumpenchants.equipment.EquipmentSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import top.theillusivec4.curios.api.CuriosApi

class CuriosEquipmentSource : EquipmentSource {
    override fun equippedStacks(player: Player): Sequence<ItemStack> {
        val inventory = CuriosApi.getCuriosInventory(player).resolve().orElse(null) ?: return emptySequence()
        val equipped = inventory.equippedCurios
        return (0 until equipped.slots).asSequence().map(equipped::getStackInSlot)
    }
}
