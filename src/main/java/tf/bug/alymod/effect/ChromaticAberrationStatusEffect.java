package tf.bug.alymod.effect;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class ChromaticAberrationStatusEffect extends StatusEffect {

    private static final Identifier MAX_HEALTH_ATTRIBUTE_IDENTIFIER =
            Identifier.of(Alymod.ID, "max_health_attribute");

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "chromatic_aberration");

    public static final ChromaticAberrationStatusEffect INSTANCE =
            new ChromaticAberrationStatusEffect();

    private final Supplier<RegistryEntry.Reference<StatusEffect>> reference =
            Suppliers.memoize(() -> Registry.registerReference(
                    Registries.STATUS_EFFECT, ChromaticAberrationStatusEffect.ID, this));
    public RegistryEntry.Reference<StatusEffect> reference() {
        return reference.get();
    }

    public ChromaticAberrationStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFFFFF);

        this.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, MAX_HEALTH_ATTRIBUTE_IDENTIFIER, -2.0d, EntityAttributeModifier.Operation.ADD_VALUE);
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
        ChromaticAberrationStatusEffect.INSTANCE.reference();
    }

}
