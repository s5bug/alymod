package tf.bug.alymod.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.block.PrismaticFluid;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;updateWaterState()Z",
                    shift = At.Shift.AFTER
            ),
            method = "baseTick()V"
    )
    public void updatePrismaticFluidState(CallbackInfo ci) {
        Entity thisx = (Entity) (Object) this;

        thisx.updateMovementInFluid(PrismaticFluid.TAG, PrismaticFluid.SPEED);
    }

}
