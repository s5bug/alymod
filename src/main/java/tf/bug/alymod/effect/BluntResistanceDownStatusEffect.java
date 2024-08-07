package tf.bug.alymod.effect;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class BluntResistanceDownStatusEffect extends StatusEffect {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "blunt_resistance_down");

    public static final BluntResistanceDownStatusEffect INSTANCE =
            new BluntResistanceDownStatusEffect();

    private final Supplier<RegistryEntry.Reference<StatusEffect>> reference =
            Suppliers.memoize(() -> Registry.registerReference(
                    Registries.STATUS_EFFECT, BluntResistanceDownStatusEffect.ID, this));
    public RegistryEntry.Reference<StatusEffect> reference() {
        return reference.get();
    }

    public BluntResistanceDownStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x992222);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return true;
    }

    public static void register() {
        BluntResistanceDownStatusEffect.INSTANCE.reference();
    }

}
