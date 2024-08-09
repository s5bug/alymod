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

public final class MonkStatusEffects extends StatusEffect {

    public static final MonkStatusEffects BLUNT_RESISTANCE_DOWN =
            new MonkStatusEffects(StatusEffectCategory.HARMFUL, 0x992222, "blunt_resistance_down");
    public static final MonkStatusEffects BROTHERHOOD =
            new MonkStatusEffects(StatusEffectCategory.BENEFICIAL, 0xffbb77, "brotherhood");
    public static final MonkStatusEffects COEURL_FORM =
            new MonkStatusEffects(StatusEffectCategory.NEUTRAL, 0xddcc55, "coeurl_form");
    public static final MonkStatusEffects DEMOLISH =
            new MonkStatusEffects(StatusEffectCategory.HARMFUL, 0xee4455, "demolish");
    public static final MonkStatusEffects FISTS_OF_EARTH =
            new MonkStatusEffects(StatusEffectCategory.NEUTRAL, 0xcc5511, "fists_of_earth");
    public static final MonkStatusEffects FISTS_OF_FIRE =
            new MonkStatusEffects(StatusEffectCategory.NEUTRAL, 0xff2720, "fists_of_fire");
    public static final MonkStatusEffects FISTS_OF_WIND =
            new MonkStatusEffects(StatusEffectCategory.NEUTRAL, 0xbbddff, "fists_of_wind");
    public static final MonkStatusEffects INTERNAL_RELEASE =
            new MonkStatusEffects(StatusEffectCategory.BENEFICIAL, 0x88dd88, "internal_release");
    public static final MonkStatusEffects MEDITATIVE_BROTHERHOOD =
            new MonkStatusEffects(StatusEffectCategory.BENEFICIAL, 0xff3311, "meditative_brotherhood");
    public static final MonkStatusEffects OPO_OPO_FORM =
            new MonkStatusEffects(StatusEffectCategory.NEUTRAL, 0xbb5577, "opo_opo_form");
    public static final MonkStatusEffects PERFECT_BALANCE =
            new MonkStatusEffects(StatusEffectCategory.NEUTRAL, 0xee9944, "perfect_balance");
    public static final MonkStatusEffects RAPTOR_FORM =
            new MonkStatusEffects(StatusEffectCategory.NEUTRAL, 0x4466cc, "raptor_form");
    public static final MonkStatusEffects RIDDLE_OF_FIRE =
            new MonkStatusEffects(StatusEffectCategory.BENEFICIAL, 0xde4a31, "riddle_of_fire");
    public static final MonkStatusEffects RIDDLE_OF_WIND =
            new MonkStatusEffects(StatusEffectCategory.BENEFICIAL, 0x0022dd, "riddle_of_wind");
    public static final MonkStatusEffects TWIN_SNAKES =
            new MonkStatusEffects(StatusEffectCategory.BENEFICIAL, 0xee88cc, "twin_snakes");

    private final Identifier id;

    private MonkStatusEffects(StatusEffectCategory category, int color, String id) {
        super(category, color);
        this.id = Identifier.of(Alymod.ID, id);
    }

    public Identifier id() {
        return this.id;
    }

    private final Supplier<RegistryEntry.Reference<StatusEffect>> reference =
            Suppliers.memoize(() -> Registry.registerReference(
                    Registries.STATUS_EFFECT, this.id(), this));
    public RegistryEntry.Reference<StatusEffect> reference() {
        return reference.get();
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
        BLUNT_RESISTANCE_DOWN.reference();
        BROTHERHOOD.reference();
        COEURL_FORM.reference();
        DEMOLISH.reference();
        FISTS_OF_EARTH.reference();
        FISTS_OF_FIRE.reference();
        FISTS_OF_WIND.reference();
        INTERNAL_RELEASE.reference();
        MEDITATIVE_BROTHERHOOD.reference();
        OPO_OPO_FORM.reference();
        PERFECT_BALANCE.reference();
        RAPTOR_FORM.reference();
        RIDDLE_OF_FIRE.reference();
        RIDDLE_OF_WIND.reference();
        TWIN_SNAKES.reference();
    }

}
