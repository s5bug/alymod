package tf.bug.alymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor
    boolean getJumping();

    @Invoker()
    float invokeGetJumpVelocity();

    @Invoker()
    void invokeOnStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source);

}
