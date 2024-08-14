package tf.bug.alymod.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatusEffectInstance.class)
public interface StatusEffectInstanceAccessor {

    @Accessor
    void setAmplifier(int amplifier);

    @Accessor
    void setDuration(int duration);

    @Accessor
    StatusEffectInstance getHiddenEffect();

    @Accessor
    void setHiddenEffect(StatusEffectInstance hidden);

}
