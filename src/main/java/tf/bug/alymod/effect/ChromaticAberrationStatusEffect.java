package tf.bug.alymod.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class ChromaticAberrationStatusEffect extends StatusEffect {

    private static final String MAX_HEALTH_ATTRIBUTE_UUID =
            "1b5ab085-adf4-4de3-8f54-6db6cc82d193";

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "chromatic_aberration");

    public static final ChromaticAberrationStatusEffect INSTANCE =
            new ChromaticAberrationStatusEffect();

    public ChromaticAberrationStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFFFFF);

        this.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, MAX_HEALTH_ATTRIBUTE_UUID, -2.0d, EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        //
    }

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT, ChromaticAberrationStatusEffect.ID, ChromaticAberrationStatusEffect.INSTANCE);
    }

}
