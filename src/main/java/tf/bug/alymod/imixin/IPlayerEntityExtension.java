package tf.bug.alymod.imixin;

import java.time.Duration;
import java.util.List;
import net.minecraft.util.math.Vec2f;
import tf.bug.alymod.monk.CooldownGroup;
import tf.bug.alymod.monk.MonkAction;

public interface IPlayerEntityExtension extends IEntityExtension {

    List<Vec2f> alymod$getBaseVelocities();

    boolean alymod$useEclipticClawImpulse();
    void alymod$resetEclipticClawImpulses();

    Duration alymod$getCooldownDuration(CooldownGroup group);
    long alymod$getTickOffCooldown(CooldownGroup group);
    float alymod$getDeltaOffCooldown(CooldownGroup group);
    void alymod$setOffCooldown(CooldownGroup group, Duration duration, long tick, float delta);

    void alymod$setQueuedAction(MonkAction action, long queuedNanos);
    long alymod$getQueuedActionNanos();
    MonkAction alymod$getQueuedAction();

}
