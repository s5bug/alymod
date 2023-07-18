package tf.bug.alymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.imixin.IEntityExtension;
import tf.bug.alymod.item.EclipticClaw;

@Mixin(Entity.class)
public class EntityMixin implements IEntityExtension {

    private int disableMovementTicks;

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

    @Override
    public int getDisableMovementTicks() {
        return this.disableMovementTicks;
    }

    @Override
    public void setDisableMovementTicks(int ticks) {
        this.disableMovementTicks = ticks;
    }

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "tick()V"
    )
    public void tick(CallbackInfo ci) {
        if(this.disableMovementTicks > 0) --this.disableMovementTicks;
    }

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "updateVelocity(FLnet/minecraft/util/math/Vec3d;)V",
            cancellable = true
    )
    public void updateVelocity(float speed, Vec3d movementInput, CallbackInfo ci) {
        Entity thisx = (Entity) (Object) this;

        if(EclipticClaw.overrideUpdateVelocity(thisx, speed, movementInput))
            ci.cancel();
    }

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "onBubbleColumnCollision(Z)V"
    )
    public void onBubbleColumnCollision(boolean drag, CallbackInfo ci) {
        this.disableMovementTicks = 2;
    }

}
