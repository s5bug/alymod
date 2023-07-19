package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tf.bug.alymod.item.EclipticClaw;

@Mixin(TridentItem.class)
public class TridentItemMixin {

    @WrapOperation(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addVelocity(DDD)V"
            ),
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"
    )
    public void redirectAddVelocity(PlayerEntity player, double x, double y, double z, Operation<Void> original) {
        if(player.getInventory().contains(EclipticClaw.INSTANCE.getDefaultStack())) {
            double currentMagnitude = player.getVelocity().length();
            Vec3d additive = new Vec3d(x, y, z);
            Vec3d redirected = additive.normalize().multiply(additive.length() + currentMagnitude);
            player.setVelocity(redirected);
        } else {
            original.call(player, x, y, z);
        }
    }

}
