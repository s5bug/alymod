package tf.bug.alymod.mixin;

import net.minecraft.item.CrossbowItem;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface CrossbowItemAccessor {
    @Invoker()
    float invokeGetSoundPitch(Random random, int index);
}
