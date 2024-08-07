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

public class RiddleOfWindStatusEffect extends StatusEffect {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "riddle_of_wind");

    public static final RiddleOfWindStatusEffect INSTANCE =
            new RiddleOfWindStatusEffect();

    private final Supplier<RegistryEntry.Reference<StatusEffect>> reference =
            Suppliers.memoize(() -> Registry.registerReference(
                    Registries.STATUS_EFFECT, RiddleOfWindStatusEffect.ID, this));
    public RegistryEntry.Reference<StatusEffect> reference() {
        return reference.get();
    }

    public RiddleOfWindStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x0022dd);
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
        RiddleOfWindStatusEffect.INSTANCE.reference();
    }

}
