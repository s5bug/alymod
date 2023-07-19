package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.item.EclipticClaw;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @WrapOperation(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInTeleportationState()Z"
            ),
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V"
    )
    public boolean dontPerformLagbackForEclipticClawHolders(ServerPlayerEntity player, Operation<Boolean> original) {
        // We return true here to cancel the movement check
        if(player.getInventory().contains(EclipticClaw.INSTANCE.getDefaultStack())) {
            return true;
        } else {
            return original.call(player);
        }
    }

}
