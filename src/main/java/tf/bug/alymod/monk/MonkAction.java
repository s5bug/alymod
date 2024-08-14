package tf.bug.alymod.monk;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.effect.MonkStatusEffects;
import tf.bug.alymod.network.MonkActionUsePayload;

public enum MonkAction {
    BOOTSHINE(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 140,
                    Duration.ofMillis(1110L),
                    (p, e) -> StatusHelpers.hasOpoOpoBonus(p) && PositionalHelpers.isRear(p, e)
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) StatusHelpers.giveRaptorForm(player);
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(-1,  0.95f),
            "bootshine"
    ),
    TRUE_STRIKE(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            StatusHelpers::hasRaptorBonus,
            StatusHelpers::isRaptorForm,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> PositionalHelpers.isRear(p, e) ? 180 : 140,
                    Duration.ofMillis(800L)
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) StatusHelpers.giveCoeurlForm(player);
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.95f),
            "true_strike"
    ),
    SNAP_PUNCH(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            StatusHelpers::hasCoeurlBonus,
            StatusHelpers::isCoeurlForm,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> PositionalHelpers.isFlank(p, e) ? 170 : 130,
                    Duration.ofMillis(760L)
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) {
                    StatusHelpers.giveOpoOpoForm(player);
                    StatusHelpers.giveGreasedLightning(player);
                }
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.76f),
            "snap_punch"
    ),
    INTERNAL_RELEASE(
            CooldownGroup.INTERNAL_RELEASE,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.DelayedAction<>(
                    Duration.ofMillis(700L),
                    (p, e) -> StatusHelpers.giveInternalRelease(e)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(60L)),
            new SoundEffectStrategy.CastTargetInstant(-1, 0.665f),
            "internal_release"
    ),
    FISTS_OF_EARTH(
            CooldownGroup.FISTS,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>(
                    (p, e) -> StatusHelpers.toggleFistsOfEarth(e)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(3L)),
            new SoundEffectStrategy.CastTargetInstant(-1, 0.57f),
            "fists_of_earth"
    ),
    TWIN_SNAKES(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            StatusHelpers::hasRaptorBonus,
            StatusHelpers::isRaptorForm,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> PositionalHelpers.isFlank(p, e) ? 130 : 100,
                    Duration.ofMillis(840L)
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) StatusHelpers.giveCoeurlForm(player);
            }).withLand((player, state) -> StatusHelpers.giveTwinSnakes(player)),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.57f, 0.855f),
            "twin_snakes"
    ),
    ARM_OF_THE_DESTROYER(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            new TargetStrategy.Spherical(5),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 50,
                    Duration.ofMillis(530L)
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) StatusHelpers.giveRaptorForm(player);
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.855f, -1),
            "arm_of_the_destroyer"
    ),
    DEMOLISH(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            StatusHelpers::hasCoeurlBonus,
            StatusHelpers::isCoeurlForm,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> PositionalHelpers.isRear(p, e) ? 70 : 30,
                    Duration.ofMillis(1600L),
                    (p, e) -> false,
                    (p, e) -> false,
                    (p, e) -> () -> new StatusEffectInstance(
                            MonkStatusEffects.DEMOLISH.reference(),
                            18 * 20
                    )
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) {
                    StatusHelpers.giveOpoOpoForm(player);
                    StatusHelpers.giveGreasedLightning(player);
                }
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.617f, 0.76f),
            "demolish"
    ),
    ROCKBREAKER(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            StatusHelpers::hasCoeurlBonus,
            StatusHelpers::isCoeurlForm,
            new TargetStrategy.Conal(5, Math.PI / 4.0D),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 130,
                    Duration.ofMillis(940L)
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) {
                    StatusHelpers.giveOpoOpoForm(player);
                    StatusHelpers.giveGreasedLightning(player);
                }
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.76f, 0.665f), // TODO implement delay on this
            "rockbreaker"
    ),
    FISTS_OF_WIND(
            CooldownGroup.FISTS,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>(
                    (p, e) -> StatusHelpers.toggleFistsOfWind(e)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(3L)),
            new SoundEffectStrategy.CastTargetInstant(0.950f, -1),
            "fists_of_wind"
    ),
    SHOULDER_TACKLE(
            MonkAction::shoulderTackleReplacements,
            CooldownGroup.TACKLES,
            ActionType.ABILITY,
            p -> !StatusHelpers.isFistsOfEarth(p) && !StatusHelpers.isFistsOfWind(p) && !StatusHelpers.isFistsOfFire(p),
            p -> false,
            new TargetStrategy.GapClose(20),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 100,
                    Duration.ofMillis(500L)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(30L)),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.95f),
            "shoulder_tackle"
    ),
    STEEL_PEAK(
            CooldownGroup.STEEL_PEAK,
            ActionType.ABILITY,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 150,
                    Duration.ofMillis(500L)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(40L)),
            new SoundEffectStrategy.CastTargetInstant(-1, 0.95f),
            "steel_peak"
    ),
    FISTS_OF_FIRE(
            CooldownGroup.FISTS,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>(
                    (p, e) -> StatusHelpers.toggleFistsOfFire(e)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(3L)),
            new SoundEffectStrategy.CastTargetInstant(-1, 0.665f),
            "fists_of_fire"
    ),
    MANTRA(
            CooldownGroup.MANTRA,
            ActionType.ABILITY,
            new TargetStrategy.Spherical(7),
            new ActionTimeline.Action<>((p, e) -> {}), // TODO figure out something with scoreboard teams maybe?
            new CooldownStrategy.Ability(Duration.ofSeconds(90L)),
            new SoundEffectStrategy.CastTargetInstant(0.7f, 0.57f),
            "mantra"
    ),
    ONE_ILM_PUNCH(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            StatusHelpers::hasRaptorBonus,
            StatusHelpers::isRaptorForm,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 120,
                    null // FIXME
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) StatusHelpers.giveCoeurlForm(player);
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.95f),
            "one_ilm_punch"
    ),
    HOWLING_FIST(
            CooldownGroup.HOWLING_FIST,
            ActionType.ABILITY,
            new TargetStrategy.Line(10),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 210,
                    Duration.ofMillis(1200L)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(60L)),
            new SoundEffectStrategy.CastTargetInstant(0.95f, -1),
            "howling_fist"
    ),
    PERFECT_BALANCE(
            CooldownGroup.PERFECT_BALANCE,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>(
                    (p, e) -> StatusHelpers.givePerfectBalance(e)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(60L)),
            new SoundEffectStrategy.CastTargetInstant(0.95f, -1),
            "perfect_balance"
    ),
    DRAGON_KICK(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> PositionalHelpers.isFlank(p, e) ? 140 : 100,
                    Duration.ofMillis(1290L),
                    (p, e) -> false,
                    (p, e) -> false,
                    (p, e) -> StatusHelpers.hasOpoOpoBonus(p) ?
                            () -> new StatusEffectInstance(
                                    MonkStatusEffects.BLUNT_RESISTANCE_DOWN.reference(),
                                    15 * 20
                            ) : null
            ).withCast((player, state) -> {
                if(!state.damage.isEmpty()) StatusHelpers.giveRaptorForm(player);
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.76f, 0.76f),
            "dragon_kick"
    ),
    FORM_SHIFT(
            CooldownGroup.GCD,
            ActionType.WEAPONSKILL,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>((p, e) -> {
                if(StatusHelpers.isOpoOpoForm(e)) {
                    StatusHelpers.giveRaptorForm(e);
                } else if(StatusHelpers.isRaptorForm(e)) {
                    StatusHelpers.giveCoeurlForm(e);
                } else {
                    StatusHelpers.giveOpoOpoForm(e);
                }
            }),
            new CooldownStrategy.Weaponskill(),
            new SoundEffectStrategy.CastTargetInstant(0.712f, -1),
            "form_shift"
    ),
    MEDITATION(
            MonkAction::meditationReplacement,
            CooldownGroup.GCD,
            ActionType.ABILITY,
            p -> StatusHelpers.getChakra(p) < 5,
            p -> false,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>((p, e) -> {
                StatusHelpers.giveChakra(p);
            }),
            new CooldownStrategy.Ability(Duration.ofMillis(1200L)),
            new SoundEffectStrategy.CastTargetInstant(0.712f, -1),
            "meditation"
    ),
    THE_FORBIDDEN_CHAKRA(
            CooldownGroup.THE_FORBIDDEN_CHAKRA,
            ActionType.ABILITY,
            p -> StatusHelpers.getChakra(p) >= 5,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 250,
                    Duration.ofMillis(1480L)
            ).withCast((p, s) -> StatusHelpers.consumeChakra(p)),
            new CooldownStrategy.Ability(Duration.ofSeconds(5L)),
            new SoundEffectStrategy.CastTargetInstant(0.855f, 0.855f),
            "the_forbidden_chakra"
    ),
    ELIXIR_FIELD(
            CooldownGroup.ELIXIR_FIELD,
            ActionType.ABILITY,
            new TargetStrategy.Spherical(5),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 220,
                    Duration.ofMillis(1080L)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(30L)),
            new SoundEffectStrategy.CastTargetInstant(0.855f, 0.665f), // TODO application delay on target
            "elixir_field"
    ),
    // TODO Purification?
    TORNADO_KICK(
            CooldownGroup.TORNADO_KICK,
            ActionType.ABILITY,
            p -> StatusHelpers.getGreasedLightning(p) >= 3,
            new TargetStrategy.Single(3),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 430,
                    Duration.ofMillis(1700L)
            ).withCast((p, s) -> StatusHelpers.consumeGreasedLightning(p)),
            new CooldownStrategy.Ability(Duration.ofSeconds(10L)),
            new SoundEffectStrategy.CastTargetInstant(0.7f, 0.57f),
            "tornado_kick"
    ),
    RIDDLE_OF_EARTH(
            CooldownGroup.RIDDLE_OF_EARTH,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>((p, e) -> {}), // TODO
            new CooldownStrategy.Ability(Duration.ofSeconds(60)),
            new SoundEffectStrategy.CastTargetInstant(0.855f, -1),
            "riddle_of_earth"
    ),
    EARTH_TACKLE(
            CooldownGroup.TACKLES,
            ActionType.ABILITY,
            StatusHelpers::isFistsOfEarth,
            new TargetStrategy.GapClose(10),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 100,
                    Duration.ofMillis(500L)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(30L)),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.95f),
            "earth_tackle"
    ),
    WIND_TACKLE(
            MonkAction::windTackleReplacement,
            CooldownGroup.TACKLES,
            ActionType.ABILITY,
            p -> StatusHelpers.isFistsOfWind(p) && !StatusHelpers.hasRiddleOfWind(p),
            p -> false,
            new TargetStrategy.GapClose(20),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 65,
                    Duration.ofMillis(500L)
            ).withCast((p, e) -> StatusHelpers.giveRiddleOfWind(p)),
            new CooldownStrategy.Ability(Duration.ofSeconds(30L)),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.95f),
            "wind_tackle"
    ),
    FIRE_TACKLE(
            CooldownGroup.TACKLES,
            ActionType.ABILITY,
            StatusHelpers::isFistsOfFire,
            new TargetStrategy.GapClose(20),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 130,
                    Duration.ofMillis(500L)
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(30L)),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.95f),
            "fire_tackle"
    ),
    RIDDLE_OF_WIND(
            CooldownGroup.RIDDLE_OF_WIND,
            ActionType.ABILITY,
            StatusHelpers::hasRiddleOfWind,
            StatusHelpers::hasRiddleOfWind,
            new TargetStrategy.GapClose(20),
            new ActionTimeline.FlatDamage(
                    (p, e) -> 65,
                    Duration.ofMillis(500L)
            ).withCast((p, s) -> {
                StatusHelpers.consumeRiddleOfWind(p);
                StatusHelpers.giveGreasedLightning(p);
            }),
            new CooldownStrategy.Ability(Duration.ofSeconds(1L)),
            new SoundEffectStrategy.CastTargetInstant(0.95f, 0.95f),
            "riddle_of_wind"
    ),
    RIDDLE_OF_FIRE(
            CooldownGroup.RIDDLE_OF_FIRE,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>((p, e) -> StatusHelpers.giveRiddleOfFire(e)),
            new CooldownStrategy.Ability(Duration.ofSeconds(90L)),
            new SoundEffectStrategy.CastTargetInstant(0.893f, -1),
            "riddle_of_fire"
    ),
    BROTHERHOOD(
            CooldownGroup.BROTHERHOOD,
            ActionType.ABILITY,
            new TargetStrategy.Spherical(15).partyOnly().join(new TargetStrategy.Self()),
            new ActionTimeline.DelayedAction<>(
                    Duration.ofMillis(700L),
                    (p, e) -> {
                        StatusHelpers.giveBrotherhood(e);
                        StatusHelpers.giveMeditativeBrotherhood(e, p);
                    }
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(90L)),
            new SoundEffectStrategy.CastTargetInstant(0.93f, 0.902f), // TODO add application delay
            "brotherhood"
    ),
    SECOND_WIND(
            CooldownGroup.SECOND_WIND,
            ActionType.ABILITY,
            new TargetStrategy.Self(),
            new ActionTimeline.Action<>((p, e) -> {}), // TODO
            new CooldownStrategy.Ability(Duration.ofSeconds(120L)),
            new SoundEffectStrategy.CastTargetInstant(0.665f, -1),
            "second_wind"
    ),
    SPRINT(
            CooldownGroup.SPRINT,
            ActionType.ABILITY,
            new TargetStrategy.Aggressors(),
            new ActionTimeline.CombatDifferenceAction<>(
                    () -> 10 * 20,
                    () -> 20 * 20,
                    StatusHelpers::giveSprint
            ),
            new CooldownStrategy.Ability(Duration.ofSeconds(60L)),
            new SoundEffectStrategy.CastTargetInstant(-1, -1),
            "sprint"
    )
    ;

    private final Function<PlayerEntity, @Nullable MonkAction> replacement;
    private final CooldownGroup cooldownGroup;
    private final ActionType actionType;
    private final Predicate<PlayerEntity> enabled;
    private final Predicate<PlayerEntity> highlighted;
    private final TargetTimelineChoreo<? extends Entity, ?> choreo;
    private final CooldownStrategy cooldownStrategy;
    private final SoundEffectStrategy soundEffectStrategy;
    private final String id;

    private final Identifier actionIconIdentifier;
    private SoundEffect registeredSoundEffect;

    <T extends Entity, S> MonkAction(
            Function<PlayerEntity, @Nullable MonkAction> replacement,
            CooldownGroup cooldownGroup,
            ActionType actionType,
            Predicate<PlayerEntity> enabled,
            Predicate<PlayerEntity> highlighted,
            TargetStrategy<T> targetStrategy,
            ActionTimeline<T, S> actionTimeline,
            CooldownStrategy cooldownStrategy,
            SoundEffectStrategy soundEffectStrategy,
            String id
    ) {
        this.replacement = replacement;
        this.cooldownGroup = cooldownGroup;
        this.actionType = actionType;
        this.enabled = enabled;
        this.highlighted = highlighted;
        this.choreo = new TargetTimelineChoreo<>(targetStrategy, actionTimeline);
        this.cooldownStrategy = cooldownStrategy;
        this.soundEffectStrategy = soundEffectStrategy;
        this.id = id;

        this.actionIconIdentifier = Identifier.of(Alymod.ID, "action/" + this.id);
    }

    <T extends Entity, S> MonkAction(
            CooldownGroup cooldownGroup,
            ActionType actionType,
            Predicate<PlayerEntity> enabled,
            Predicate<PlayerEntity> highlighted,
            TargetStrategy<T> targetStrategy,
            ActionTimeline<T, S> actionTimeline,
            CooldownStrategy cooldownStrategy,
            SoundEffectStrategy soundEffectStrategy,
            String id
    ) {
        this(p -> null, cooldownGroup, actionType,
                enabled, highlighted,
                targetStrategy, actionTimeline, cooldownStrategy,
                soundEffectStrategy, id);
    }

    <T extends Entity, S> MonkAction(
            CooldownGroup cooldownGroup,
            ActionType actionType,
            Predicate<PlayerEntity> enabled,
            TargetStrategy<T> targetStrategy,
            ActionTimeline<T, S> actionTimeline,
            CooldownStrategy cooldownStrategy,
            SoundEffectStrategy soundEffectStrategy,
            String id
    ) {
        this(cooldownGroup, actionType, enabled, p -> false,
                targetStrategy, actionTimeline, cooldownStrategy,
                soundEffectStrategy, id);
    }

    <T extends Entity, S> MonkAction(
            CooldownGroup cooldownGroup,
            ActionType actionType,
            TargetStrategy<T> targetStrategy,
            ActionTimeline<T, S> actionTimeline,
            CooldownStrategy cooldownStrategy,
            SoundEffectStrategy soundEffectStrategy,
            String id
    ) {
        this(cooldownGroup, actionType, p -> true,
                targetStrategy, actionTimeline, cooldownStrategy,
                soundEffectStrategy, id);
    }

    private void registerInstance() {
        this.registeredSoundEffect = this.soundEffectStrategy.register(this.id);
    }

    public Identifier getActionIcon() {
        return this.actionIconIdentifier;
    }

    public CooldownGroup getCooldownGroup() {
        return this.cooldownGroup;
    }

    public boolean isEnabled(PlayerEntity player) {
        return this.enabled.test(player);
    }

    public boolean isHighlighted(PlayerEntity player) {
        return this.highlighted.test(player);
    }

    @NotNull
    public MonkAction getFullReplacement(PlayerEntity player) {
        MonkAction result = this;
        MonkAction next;
        while((next = result.replacement.apply(player)) != null) {
            result = next;
        }
        return result;
    }

    @Environment(EnvType.CLIENT)
    public Duration getRecast(ClientPlayerEntity player) {
        return this.cooldownStrategy.getCooldown(player, MonkStats.Lv70.INSTANCE);
    }

    @Environment(EnvType.CLIENT)
    public boolean tryExecuteClient(ClientPlayerEntity player, float tickDelta) {
        TargetStrategy.Result<? extends Entity> result = this.choreo.strategy().attemptTarget(player, tickDelta);
        if(!result.castSucceeded()) return false;

        List<? extends Entity> targets = result.targets();
        ClientPlayNetworking.send(new MonkActionUsePayload(
                this.ordinal(),
                targets.stream().map(Entity::getId).toList()
        ));

        this.registeredSoundEffect.clientOnSnapshotSelf(player);
        for(Entity target : targets) {
            this.registeredSoundEffect.clientOnSnapshotTarget(player, target);
        }

        return true;
    }

    public void executeServer(ServerPlayerEntity player, List<Entity> targets) {
        this.choreo.executeServer(player, targets);

        // In Stormblood, Meditative Brotherhood scaled based off of number of targets
        // Due to lacking a time machine, it's impossible to know whether or not
        // - a 0-hit AoE would count as 0 possible generations or 1
        // - Form Shift would count as 0 possible generations or 1
        // For the purposes of this sim, we will directly use the number of targets:
        // - a 0-hit AoE will count as 0 possible generations
        // - Form Shift will count as 1 possible generation
        if(this.actionType == ActionType.WEAPONSKILL &&
                player.hasStatusEffect(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.reference())) {
            ExtendedStatusEffectInstance<Object2IntMap<UUID>> bh =
                    new ExtendedStatusEffectInstance<>(
                            player.getStatusEffect(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.reference())
                    );
            List<ServerPlayerEntity> grantChakraTo =
                    bh.getExtension().keySet().stream()
                            .mapMulti((UUID uuid, Consumer<ServerPlayerEntity> consumer) -> {
                                Entity bhApplier = player.getServerWorld().getPlayerByUuid(uuid);
                                if (bhApplier instanceof ServerPlayerEntity spe) consumer.accept(spe);
                            }).toList();
            Random random = player.getRandom();
            for(Entity ignored : targets) {
                for(ServerPlayerEntity spe : grantChakraTo) {
                    // 30% chance
                    if(random.nextDouble() < 0.3) {
                        StatusHelpers.giveChakra(spe);
                    }
                }
            }
        }

        this.registeredSoundEffect.serverOnSnapshotSelf(player);
        for(Entity target : targets) {
            this.registeredSoundEffect.serverOnSnapshotTarget(player, target);
        }
    }

    public static void register() {
        for (MonkAction action : MonkAction.values()) {
            action.registerInstance();
        }
    }

    private static MonkAction meditationReplacement(PlayerEntity pe) {
        if(StatusHelpers.getChakra(pe) >= 5) return MonkAction.THE_FORBIDDEN_CHAKRA;
        return null;
    }

    private static MonkAction shoulderTackleReplacements(PlayerEntity pe) {
        if(StatusHelpers.isFistsOfEarth(pe)) return MonkAction.EARTH_TACKLE;
        if(StatusHelpers.isFistsOfWind(pe)) return MonkAction.WIND_TACKLE;
        if(StatusHelpers.isFistsOfFire(pe)) return MonkAction.FIRE_TACKLE;
        return null;
    }

    private static MonkAction windTackleReplacement(PlayerEntity pe) {
        if(StatusHelpers.hasRiddleOfWind(pe)) return MonkAction.RIDDLE_OF_WIND;
        return null;
    }

}
