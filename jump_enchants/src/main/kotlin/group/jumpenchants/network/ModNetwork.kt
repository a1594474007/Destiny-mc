package group.jumpenchants.network

import group.jumpenchants.JumpEnchants
import group.jumpenchants.network.c2s.InputStatePacket
import group.jumpenchants.network.c2s.InputStatePacketHandler
import group.jumpenchants.network.s2c.ActionResultPacket
import group.jumpenchants.network.s2c.ActionResultPacketHandler
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel
import java.util.Optional

object ModNetwork {
    private const val PROTOCOL = "2"
    private val channel: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation(JumpEnchants.ID, "main"),
        { PROTOCOL },
        PROTOCOL::equals,
        PROTOCOL::equals
    )
    private var messageId = 0

    fun register() {
        channel.registerMessage(
            messageId++,
            InputStatePacket::class.java,
            InputStatePacket::encode,
            InputStatePacket::decode,
            InputStatePacketHandler::handle,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        )
        channel.registerMessage(
            messageId++,
            ActionResultPacket::class.java,
            ActionResultPacket::encode,
            ActionResultPacket::decode,
            ActionResultPacketHandler::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
    }

    fun sendInput(packet: InputStatePacket) {
        channel.sendToServer(packet)
    }

    internal fun sendToPlayer(player: ServerPlayer, packet: ActionResultPacket) {
        channel.send(
            PacketDistributor.PLAYER.with { player },
            packet
        )
    }
}
