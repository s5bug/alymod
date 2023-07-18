package tf.bug.alymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.bug.alymod.item.EclipticClaw;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "computeFallDamage(FF)I",
            cancellable = true
    )
    public void negateSomeEclipticClawFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity thisx = (LivingEntity) (Object) this;

        if(thisx.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) return;

        if(thisx instanceof PlayerEntity player) {
            if(player.getInventory().contains(EclipticClaw.INSTANCE.getDefaultStack())) {
                cir.setReturnValue(EclipticClaw.fallDamage(player, fallDistance, damageMultiplier));
                cir.cancel();
            }
        }
    }

}
