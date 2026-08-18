package group.jumpenchants.registry

import group.jumpenchants.JumpEnchants
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.RangedAttribute
import net.minecraftforge.event.entity.EntityAttributeModificationEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries

object ModAttributes {
    val REGISTRY: DeferredRegister<Attribute> =
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, JumpEnchants.ID)

    val AGILITY = REGISTRY.register("agility") {
        RangedAttribute("attribute.name.${JumpEnchants.ID}.agility", 0.0, 0.0, 1024.0).setSyncable(true)
    }

    fun addPlayerAttributes(event: EntityAttributeModificationEvent) {
        event.add(EntityType.PLAYER, AGILITY.get())
    }
}

