package group.jumpenchants

import group.jumpenchants.client.ClientBootstrap
import group.jumpenchants.config.JumpConfig
import group.jumpenchants.event.CommonEvents
import group.jumpenchants.network.ModNetwork
import group.jumpenchants.registry.ModAttributes
import group.jumpenchants.registry.ModEnchantments
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.registerConfig
import thedarkcolour.kotlinforforge.forge.runWhenOn

@Mod(JumpEnchants.ID)
object JumpEnchants {
    const val ID = "jump_enchants"
    val LOGGER = LogManager.getLogger(ID)

    init {
        ModEnchantments.REGISTRY.register(MOD_BUS)
        ModAttributes.REGISTRY.register(MOD_BUS)
        MOD_BUS.addListener(ModAttributes::addPlayerAttributes)
        registerConfig(ModConfig.Type.SERVER, JumpConfig.SPEC)

        ModNetwork.register()
        FORGE_BUS.register(CommonEvents)
        runWhenOn(Dist.CLIENT) { ClientBootstrap.register() }

        LOGGER.info("Jump Enchants is loading")
    }
}

