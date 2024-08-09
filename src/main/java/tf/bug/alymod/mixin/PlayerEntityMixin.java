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
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.item.EclipticClaw;
import tf.bug.alymod.monk.CooldownGroup;
import tf.bug.alymod.monk.MonkAction;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends EntityMixin implements IPlayerEntityExtension {

    // FIXME when initializers are fixed, move this stuff back to initializers
    // = new ArrayList<>()
    @Unique
    private ArrayList<Vec2f> baseVelocities;

    // FIXME = EclipticClaw.MAX_IMPULSES;
    @Unique
    private int impulsesLeft;

    @Override
    public List<Vec2f> alymod$getBaseVelocities() {
        if(this.baseVelocities == null) {
            this.baseVelocities = new ArrayList<>();
        }
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

    // FIXME = new Duration[CooldownGroup.values().length]
    @Unique
    private Duration[] cooldownDurations;
    // FIXME = new long[CooldownGroup.values().length]
    @Unique
    private long[] tickOffCooldowns;
    // FIXME = new float[CooldownGroup.values().length]
    @Unique
    private float[] deltaOffCooldowns;

    public Duration alymod$getCooldownDuration(CooldownGroup group) {
        if(this.cooldownDurations == null) {
            this.cooldownDurations = new Duration[CooldownGroup.values().length];
        }
        return this.cooldownDurations[group.ordinal()];
    }

    @Override
    public long alymod$getTickOffCooldown(CooldownGroup group) {
        if(this.tickOffCooldowns == null) {
            this.tickOffCooldowns = new long[CooldownGroup.values().length];
        }
        return this.tickOffCooldowns[group.ordinal()];
    }

    @Override
    public float alymod$getDeltaOffCooldown(CooldownGroup group) {
        if(this.deltaOffCooldowns == null) {
            this.deltaOffCooldowns = new float[CooldownGroup.values().length];
        }
        return this.deltaOffCooldowns[group.ordinal()];
    }

    @Override
    public void alymod$setOffCooldown(CooldownGroup group, Duration duration, long tick, float delta) {
        if(this.cooldownDurations == null) {
            this.cooldownDurations = new Duration[CooldownGroup.values().length];
        }
        this.cooldownDurations[group.ordinal()] = duration;
        if(this.tickOffCooldowns == null) {
            this.tickOffCooldowns = new long[CooldownGroup.values().length];
        }
        this.tickOffCooldowns[group.ordinal()] = tick;
        if(this.deltaOffCooldowns == null) {
            this.deltaOffCooldowns = new float[CooldownGroup.values().length];
        }
        this.deltaOffCooldowns[group.ordinal()] = delta;
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
