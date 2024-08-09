package tf.bug.alymod.monk;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import tf.bug.alymod.imixin.IMinecraftServerExtension;

public record TargetTimelineChoreo<T extends Entity, S>(
        TargetStrategy<T> strategy,
        ActionTimeline<T, S> timeline
) {

    @SuppressWarnings("unchecked")
    public void executeServer(final ServerPlayerEntity player, List<Entity> targetsRaw) {
        // We know that this will be valid
        final List<T> targets = (List<T>) targetsRaw;

        final S state = timeline.emptyMutable();
        for(T target : targets) {
            timeline.snapshot(player, MonkStats.Lv70.INSTANCE, target, state);
        }
        timeline.cast(player, state);

        ServerWorld sw = player.getServerWorld();
        long now = sw.getTime();
        long delayTicks = timeline.applicationDelay().toMillis() / 50L;
        final long targetTick = now + delayTicks;

        IMinecraftServerExtension serverExt = (IMinecraftServerExtension) player.getServer();
        PriorityQueue<IMinecraftServerExtension.FutureTickRunnable> futureQueue =
                serverExt.alymod$getFutureTickRunnableQueue(sw.getRegistryKey());

        final Iterator<T> targetIterator = targets.iterator();
        final Duration delayDuration = Duration.ZERO;
        IMinecraftServerExtension.FutureTickRunnable start = new IMinecraftServerExtension.FutureTickRunnable(
                targetTick,
                () -> TargetTimelineChoreo.damageApplicationStep(
                        player, futureQueue,
                        this.timeline, state, false,
                        targetIterator, targetTick, delayDuration
                )
        );
        futureQueue.add(start);
    }

    private static <T extends Entity, S> void damageApplicationStep(
            final ServerPlayerEntity player,
            final PriorityQueue<IMinecraftServerExtension.FutureTickRunnable> futureQueue,
            final ActionTimeline<T, S> actionTimeline,
            final S state,
            boolean landYet,
            final Iterator<T> targetIterator,
            final long applyStartTick,
            final Duration delayDuration
    ) {
        T appliedEntity = null;
        while(targetIterator.hasNext()) {
            T possibleTarget = targetIterator.next();
            if(possibleTarget.isAlive()) {
                appliedEntity = possibleTarget;
                break;
            }
        }
        if(appliedEntity == null) return;

        if(!landYet) {
            actionTimeline.land(player, state);
        }

        actionTimeline.apply(player, appliedEntity, state);

        Duration nextTime = delayDuration.plusMillis(130L);
        long targetTick = applyStartTick + (nextTime.toMillis() / 50L);

        IMinecraftServerExtension.FutureTickRunnable next = new IMinecraftServerExtension.FutureTickRunnable(
                targetTick,
                () -> TargetTimelineChoreo.damageApplicationStep(
                        player, futureQueue,
                        actionTimeline, state, true,
                        targetIterator, applyStartTick, delayDuration
                )
        );
        futureQueue.add(next);
    }

}
