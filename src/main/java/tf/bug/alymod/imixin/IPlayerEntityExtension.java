package tf.bug.alymod.imixin;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import net.minecraft.util.math.Vec2f;
import tf.bug.alymod.MonkAction;

public interface IPlayerEntityExtension extends IEntityExtension {

    List<Vec2f> alymod$getBaseVelocities();

    boolean alymod$useEclipticClawImpulse();
    void alymod$resetEclipticClawImpulses();

    Duration alymod$getCooldownDuration(int group);
    long alymod$getTickOffCooldown(int group);
    float alymod$getDeltaOffCooldown(int group);
    void alymod$setOffCooldown(int group, Duration duration, long tick, float delta);

    void alymod$setQueuedAction(MonkAction action, long queuedNanos);
    long alymod$getQueuedActionNanos();
    MonkAction alymod$getQueuedAction();

}
