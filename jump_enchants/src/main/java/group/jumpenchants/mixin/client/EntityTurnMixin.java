package group.jumpenchants.mixin.client;

import group.jumpenchants.client.ViewLockController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTurnMixin {
    @Inject(
        method = {"turn", "m_19884_"},
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void jumpEnchants$limitAbilityView(
        double mouseX,
        double mouseY,
        CallbackInfo callback
    ) {
        if ((Object) this instanceof LocalPlayer player &&
            ViewLockController.applyLockedTurn(player, mouseX, mouseY)) {
            callback.cancel();
        }
    }
}
