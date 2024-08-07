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

public class DemolishStatusEffect extends StatusEffect {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "demolish");

    public static final DemolishStatusEffect INSTANCE =
            new DemolishStatusEffect();

    private final Supplier<RegistryEntry.Reference<StatusEffect>> reference =
            Suppliers.memoize(() -> Registry.registerReference(
                    Registries.STATUS_EFFECT, DemolishStatusEffect.ID, this));
    public RegistryEntry.Reference<StatusEffect> reference() {
        return reference.get();
    }

    public DemolishStatusEffect() {
        // FIXME I don't really know what color Demolish should be
        super(StatusEffectCategory.HARMFUL, 0xee4455);
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
        DemolishStatusEffect.INSTANCE.reference();
    }

}
