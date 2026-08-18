package group.jumpenchants.registry

import group.jumpenchants.JumpEnchants
import group.jumpenchants.movement.MobilityMode
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import java.util.EnumMap

object ModEnchantments {
    val REGISTRY: DeferredRegister<Enchantment> =
        DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, JumpEnchants.ID)

    private val enchantments = EnumMap<MobilityMode, RegistryObject<Enchantment>>(MobilityMode::class.java)

    init {
        for (mode in MobilityMode.entries) {
            enchantments[mode] = REGISTRY.register(mode.registryName) { JumpEnchantment() }
        }
    }

    fun get(mode: MobilityMode): Enchantment = enchantments.getValue(mode).get()
}

