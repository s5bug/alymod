package tf.bug.alymod.monk;

import net.minecraft.entity.effect.StatusEffectInstance;
import tf.bug.alymod.imixin.IStatusEffectInstanceExtension;
import tf.bug.alymod.mixin.StatusEffectInstanceAccessor;

public final record ExtendedStatusEffectInstance<T>(
        StatusEffectInstance internalInstance
) {
    @SuppressWarnings("unchecked")
    public T getExtension() {
        return (T) ((IStatusEffectInstanceExtension) internalInstance).alymod$getExtension();
    }

    public void setExtension(T extension) {
        ((IStatusEffectInstanceExtension) internalInstance).alymod$setExtension(extension);
    }

    public int getAmplifier() {
        return internalInstance.getAmplifier();
    }

    public void setAmplifier(int amplifier) {
        ((StatusEffectInstanceAccessor) internalInstance).setAmplifier(amplifier);
    }

    public int getDuration() {
        return internalInstance.getDuration();
    }

    public void setDuration(int duration) {
        ((StatusEffectInstanceAccessor) internalInstance).setDuration(duration);
    }
}
