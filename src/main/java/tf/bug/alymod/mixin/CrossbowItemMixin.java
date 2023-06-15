package tf.bug.alymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.bug.alymod.item.AmethystBolt;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(
            at = @At("HEAD"),
            cancellable = true,
            method = "getSpeed(Lnet/minecraft/item/ItemStack;)F"
    )
    private static void getSpeed(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if(stack.isOf(AmethystBolt.INSTANCE)) {
            cir.setReturnValue(4.2F);
            cir.cancel();
            return;
        }
    }

    @Inject(
            at = @At("HEAD"),
            cancellable = true,
            method = "shoot"
    )
    private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci) {
        if(!world.isClient) {
            if(projectile.isOf(AmethystBolt.INSTANCE)) {
                AmethystBolt.shoot(world, shooter, hand, crossbow, projectile, soundPitch, creative, speed, divergence, simulated);

                ci.cancel();
                return;
            }
        }
    }

}
