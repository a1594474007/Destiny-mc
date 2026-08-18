package group.jumpenchants.equipment

import group.jumpenchants.JumpEnchants
import group.jumpenchants.movement.MobilityMode
import group.jumpenchants.registry.ModEnchantments
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraftforge.fml.ModList

object EquipmentResolver {
    private val vanillaArmor = EquipmentSource { player -> player.armorSlots.asSequence() }

    private val curios: EquipmentSource? by lazy {
        if (!ModList.get().isLoaded("curios")) {
            null
        } else {
            runCatching {
                val type = Class.forName("group.jumpenchants.compat.curios.CuriosEquipmentSource")
                type.getDeclaredConstructor().newInstance() as EquipmentSource
            }.onFailure {
                JumpEnchants.LOGGER.error("Curios is loaded but its Jump Enchants adapter failed", it)
            }.getOrNull()
        }
    }

    fun resolve(player: Player): MobilityMode? {
        val stacks = buildList {
            addAll(vanillaArmor.equippedStacks(player).filterNot(ItemStack::isEmpty))
            val curiosStacks = runCatching {
                curios?.equippedStacks(player)?.filterNot(ItemStack::isEmpty)?.toList().orEmpty()
            }.onFailure {
                JumpEnchants.LOGGER.debug("Unable to inspect Curios equipment for {}", player.scoreboardName, it)
            }.getOrDefault(emptyList())
            addAll(curiosStacks)
        }

        return MobilityMode.PRIORITY_ORDER.firstOrNull { mode ->
            val enchantment = ModEnchantments.get(mode)
            stacks.any { EnchantmentHelper.getItemEnchantmentLevel(enchantment, it) > 0 }
        }
    }
}
