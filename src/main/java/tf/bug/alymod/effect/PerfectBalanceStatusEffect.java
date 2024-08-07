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

public class PerfectBalanceStatusEffect extends StatusEffect {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "perfect_balance");

    public static final PerfectBalanceStatusEffect INSTANCE =
            new PerfectBalanceStatusEffect();

    private final Supplier<RegistryEntry.Reference<StatusEffect>> reference =
            Suppliers.memoize(() -> Registry.registerReference(
                    Registries.STATUS_EFFECT, PerfectBalanceStatusEffect.ID, this));
    public RegistryEntry.Reference<StatusEffect> reference() {
        return reference.get();
    }

    public PerfectBalanceStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xee9944);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return super.applyUpdateEffect(entity, amplifier);
    }

    public static void register() {
        PerfectBalanceStatusEffect.INSTANCE.reference();
    }

}
