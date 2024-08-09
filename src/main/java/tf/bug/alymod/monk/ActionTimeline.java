package tf.bug.alymod.monk;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.damage.StrikingDamage;
import tf.bug.alymod.effect.MonkStatusEffects;

public interface ActionTimeline<T extends Entity, S> {

    S emptyMutable();
    void snapshot(ServerPlayerEntity player, MonkStats stats, T target, S state);
    default void cast(ServerPlayerEntity player, S state) {}

    Duration applicationDelay();

    void apply(ServerPlayerEntity player, T target, S state);
    default void land(ServerPlayerEntity player, S state) {}

    default ActionTimeline<T, S> withCast(final BiConsumer<ServerPlayerEntity, S> action) {
        return new Extension<>(this, List.of(action), List.of());
    }

    default ActionTimeline<T, S> withLand(final BiConsumer<ServerPlayerEntity, S> action) {
        return new Extension<>(this, List.of(), List.of(action));
    }

    public static boolean snapshotCriticalHit(PlayerEntity player, MonkStats stats, Random r) {
        double internalRelease =
                player.hasStatusEffect(MonkStatusEffects.INTERNAL_RELEASE.reference()) ? 0.3 : 0.0;
        return r.nextDouble() <= (internalRelease + stats.getProbabilityCriticalHit());
    }

    public static boolean snapshotDirectHit(PlayerEntity player, MonkStats stats, Random r) {
        return r.nextDouble() <= stats.getProbabilityDirectHit();
    }

    public static int snapshotDamage(PlayerEntity player, MonkStats stats,
                                     int potency, boolean criticalHit, boolean directHit,
                                     double buffDebuffMultiplier, Random r) {
        int d1 = (int) Math.floor(potency * stats.getMultiplierAttackPower() * stats.getMultiplierDetermination());
        int d2 = Math.floorDiv(d1 * stats.getMultiplierWeaponDamage(), 100);

        double critMulti = criticalHit ? stats.getMultiplierCrit() : 1.0d;
        double dhMulti = directHit ? 1.25d : 1.0d;

        int d3 = (int) Math.floor(d2 * critMulti * dhMulti);

        double variance = 0.95d + (0.1d * r.nextDouble());
        int d4 = (int) Math.floor(d3 * variance * buffDebuffMultiplier);

        return d4;
    }

    public static double getBuffDamageBonus(PlayerEntity player) {
        double glBonus =
                (100 + (10 * StatusHelpers.getGreasedLightning(player))) / 100.0d;
        double twinSnakesBonus =
                player.hasStatusEffect(MonkStatusEffects.TWIN_SNAKES.reference()) ? 1.10 : 1.00;
        double rofBonus =
                player.hasStatusEffect(MonkStatusEffects.RIDDLE_OF_FIRE.reference()) ? 1.30 : 1.00;
        double fofBonus =
                player.hasStatusEffect(MonkStatusEffects.FISTS_OF_FIRE.reference()) ? 1.06 : 1.00;

        return glBonus * twinSnakesBonus * rofBonus * fofBonus;
    }

    public static double getDebuffDamageBonus(LivingEntity target) {
        double bluntResistBonus =
                target.hasStatusEffect(MonkStatusEffects.BLUNT_RESISTANCE_DOWN.reference()) ? 1.10 : 1.00;
        return bluntResistBonus;
    }

    public static final record FlatDamage(ToIntBiFunction<ServerPlayerEntity, Entity> potency,
                                          Duration applicationDelay,
                                          BiPredicate<ServerPlayerEntity, Entity> guaranteedCrit,
                                          BiPredicate<ServerPlayerEntity, Entity> guaranteedDhit,
                                          BiFunction<ServerPlayerEntity, Entity, @Nullable Supplier<StatusEffectInstance>> debuff) implements ActionTimeline<Entity, FlatDamage.State> {
        public FlatDamage(ToIntBiFunction<ServerPlayerEntity, Entity> potency, Duration applicationDelay) {
            this(potency, applicationDelay, (p, e) -> false);
        }
        public FlatDamage(ToIntBiFunction<ServerPlayerEntity, Entity> potency,
                          Duration applicationDelay,
                          BiPredicate<ServerPlayerEntity, Entity> guaranteedCrit) {
            this(potency, applicationDelay, guaranteedCrit, (p, e) -> false, null);
        }
        public FlatDamage(ToIntBiFunction<ServerPlayerEntity, Entity> potency,
                          Duration applicationDelay,
                          BiPredicate<ServerPlayerEntity, Entity> guaranteedCrit,
                          BiPredicate<ServerPlayerEntity, Entity> guaranteedDhit) {
            this(potency, applicationDelay, guaranteedCrit, guaranteedDhit, (p, e) -> null);
        }

        public static final class State {
            public boolean givenChakra = false;
            public final Object2IntMap<Entity> damage = new Object2IntOpenHashMap<>();
            public final Set<Entity> crits = new HashSet<>();
        }

        @Override
        public State emptyMutable() {
            return new State();
        }

        @Override
        public void snapshot(ServerPlayerEntity player, MonkStats stats, Entity target, State state) {
            int potency = this.potency.applyAsInt(player, target);
            double buffs = getBuffDamageBonus(player);
            double debuffs = target instanceof LivingEntity living ? getDebuffDamageBonus(living) : 1.0d;
            boolean crit = guaranteedCrit.test(player, target) || snapshotCriticalHit(player, stats, player.getRandom());
            boolean dhit = guaranteedDhit.test(player, target) || snapshotDirectHit(player, stats, player.getRandom());

            final int damage = snapshotDamage(
                    player, stats, potency, crit, dhit,
                    buffs * debuffs, player.getRandom());

            state.damage.put(target, damage);
            if(crit) state.crits.add(target);
        }

        @Override
        public void apply(ServerPlayerEntity player, Entity target, State state) {
            if(state.crits.contains(target) && !state.givenChakra && player.getRandom().nextDouble() < 0.5d) {
                StatusHelpers.giveChakra(player);
                state.givenChakra = true;
            }

            // TODO log and stuff
            int damage = state.damage.getInt(target);
            target.damage(
                    player.getServerWorld().getDamageSources().create(StrikingDamage.KEY, player),
                    damage / 50.0F
            );
        }
    }

    public static final record Action<T extends Entity>(
            BiConsumer<ServerPlayerEntity, T> onSnapshot
    ) implements ActionTimeline<T, Void> {
        @Override
        public Void emptyMutable() {
            return null;
        }

        @Override
        public void snapshot(ServerPlayerEntity player, MonkStats stats, T target, Void state) {
            onSnapshot.accept(player, target);
        }

        @Override
        public Duration applicationDelay() {
            return Duration.ZERO;
        }

        @Override
        public void apply(ServerPlayerEntity player, T target, Void state) {}
    }

    public static final record DelayedAction<T extends Entity>(
            Duration applicationDelay,
            BiConsumer<ServerPlayerEntity, T> onApply
    ) implements ActionTimeline<T, Void> {
        @Override
        public Void emptyMutable() {
            return null;
        }

        @Override
        public void snapshot(ServerPlayerEntity player, MonkStats stats, T target, Void state) {}

        @Override
        public void apply(ServerPlayerEntity player, T target, Void state) {
            onApply.accept(player, target);
        }
    }

    static final record Extension<T extends Entity, S>(ActionTimeline<T, S> original,
                                     List<BiConsumer<ServerPlayerEntity, S>> castFunctions,
                                     List<BiConsumer<ServerPlayerEntity, S>> landFunctions) implements ActionTimeline<T, S> {
        @Override
        public S emptyMutable() {
            return original.emptyMutable();
        }

        @Override
        public void snapshot(ServerPlayerEntity player, MonkStats stats, T target, S state) {
            original.snapshot(player, stats, target, state);
        }

        @Override
        public void cast(final ServerPlayerEntity player, final S state) {
            original.cast(player, state);
            castFunctions.forEach(c -> c.accept(player, state));
        }

        @Override
        public Duration applicationDelay() {
            return original.applicationDelay();
        }

        @Override
        public void apply(ServerPlayerEntity player, T target, S state) {
            original.apply(player, target, state);
        }

        @Override
        public void land(final ServerPlayerEntity player, final S state) {
            original.land(player, state);
            landFunctions.forEach(c -> c.accept(player, state));
        }

        @Override
        public ActionTimeline<T, S> withCast(BiConsumer<ServerPlayerEntity, S> action) {
            ArrayList<BiConsumer<ServerPlayerEntity, S>> newCastFunctions = new ArrayList<>(castFunctions);
            newCastFunctions.add(action);
            return new Extension<>(original, newCastFunctions, landFunctions);
        }

        @Override
        public ActionTimeline<T, S> withLand(BiConsumer<ServerPlayerEntity, S> action) {
            ArrayList<BiConsumer<ServerPlayerEntity, S>> newLandFunctions = new ArrayList<>(landFunctions);
            newLandFunctions.add(action);
            return new Extension<>(original, castFunctions, newLandFunctions);
        }
    }

}
