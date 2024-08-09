package tf.bug.alymod.imixin;

import java.util.PriorityQueue;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface IMinecraftServerExtension {

    PriorityQueue<FutureTickRunnable> alymod$getFutureTickRunnableQueue(RegistryKey<World> world);

    record FutureTickRunnable(long target, Runnable action) implements Comparable<FutureTickRunnable> {

        @Override
        public int compareTo(@NotNull IMinecraftServerExtension.FutureTickRunnable o) {
            return Long.compare(target, o.target);
        }

    }
}
