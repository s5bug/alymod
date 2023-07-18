package tf.bug.alymod.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.item.EclipticClaw;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends EntityMixin implements IPlayerEntityExtension {

    private ArrayList<Vec2f> baseVelocities = new ArrayList<>();

    private int impulsesLeft = EclipticClaw.MAX_IMPULSES;

    @Override
    public List<Vec2f> getBaseVelocities() {
        return this.baseVelocities;
    }

    @Override
    public boolean useEclipticClawImpulse() {
        if(this.impulsesLeft > 0) {
            this.impulsesLeft--;
            return true;
        } else return false;
    }

    @Override
    public void resetEclipticClawImpulses() {
        this.impulsesLeft = EclipticClaw.MAX_IMPULSES;
    }

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "travel(Lnet/minecraft/util/math/Vec3d;)V",
            cancellable = true
    )
    public void travel(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity thisx = (PlayerEntity) (Object) this;

        if(EclipticClaw.overrideTravel(thisx, movementInput))
            ci.cancel();
    }

    @Inject(
            at = @At(
                    value = "TAIL"
            ),
            method = "jump()V"
    )
    public void jump(CallbackInfo ci) {
        PlayerEntity thisx = (PlayerEntity) (Object) this;

        EclipticClaw.afterJump(thisx);
    }

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "tick()V"
    )
    public void tick(CallbackInfo ci) {
        PlayerEntity thisx = (PlayerEntity) (Object) this;

        if(thisx.isOnGround() || thisx.isClimbing() || thisx.isTouchingWater() || thisx.isInLava()) {
            this.resetEclipticClawImpulses();
        }

        EclipticClaw.beforeTick(thisx);
    }

}
