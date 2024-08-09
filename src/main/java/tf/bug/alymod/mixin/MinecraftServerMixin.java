package tf.bug.alymod.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tf.bug.alymod.imixin.IMinecraftServerExtension;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IMinecraftServerExtension {

    @Unique
    private final Map<RegistryKey<World>, PriorityQueue<FutureTickRunnable>> futureTickRunnables =
            new HashMap<>();

    @Override
    public PriorityQueue<FutureTickRunnable> alymod$getFutureTickRunnableQueue(RegistryKey<World> world) {
        return futureTickRunnables.computeIfAbsent(world, w -> new PriorityQueue<>());
    }

}
