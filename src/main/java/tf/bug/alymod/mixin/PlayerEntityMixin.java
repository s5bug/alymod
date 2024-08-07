package tf.bug.alymod.mixin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.MonkAction;
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.item.EclipticClaw;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends EntityMixin implements IPlayerEntityExtension {

    @Unique
    private final ArrayList<Vec2f> baseVelocities = new ArrayList<>();

    @Unique
    private int impulsesLeft = EclipticClaw.MAX_IMPULSES;

    @Override
    public List<Vec2f> alymod$getBaseVelocities() {
        return this.baseVelocities;
    }

    @Override
    public boolean alymod$useEclipticClawImpulse() {
        if(this.impulsesLeft > 0) {
            this.impulsesLeft--;
            return true;
        } else return false;
    }

    @Override
    public void alymod$resetEclipticClawImpulses() {
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
            this.alymod$resetEclipticClawImpulses();
        }

        EclipticClaw.beforeTick(thisx);
    }

    @Unique
    private final Duration[] cooldownDurations = new Duration[16];
    @Unique
    private final long[] tickOffCooldowns = new long[16];
    @Unique
    private final float[] deltaOffCooldowns = new float[16];

    public Duration alymod$getCooldownDuration(int group) {
        return cooldownDurations[group];
    }

    @Override
    public long alymod$getTickOffCooldown(int group) {
        return tickOffCooldowns[group];
    }

    @Override
    public float alymod$getDeltaOffCooldown(int group) {
        return deltaOffCooldowns[group];
    }

    @Override
    public void alymod$setOffCooldown(int group, Duration duration, long tick, float delta) {
        cooldownDurations[group] = duration;
        tickOffCooldowns[group] = tick;
        deltaOffCooldowns[group] = delta;
    }

    @Unique
    private MonkAction queuedAction;
    @Unique
    private long queuedNanos;

    @Override
    public void alymod$setQueuedAction(MonkAction action, long queuedNanos) {
        this.queuedAction = action;
        this.queuedNanos = queuedNanos;
    }

    @Override
    public MonkAction alymod$getQueuedAction() {
        return this.queuedAction;
    }

    @Override
    public long alymod$getQueuedActionNanos() {
        return this.queuedNanos;
    }
}
