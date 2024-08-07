package tf.bug.alymod;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import tf.bug.alymod.attachment.PlayerMonkAttachments;
import tf.bug.alymod.effect.*;
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.network.MonkActionUsePayload;
import tf.bug.alymod.network.MonkAttachmentUpdatePayload;

public enum MonkAction {
    DRAGON_KICK(
            0,
            3,
            0,
            Identifier.of(Alymod.ID, "action/dragon_kick")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isOpoOpoForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            boolean inflictBluntDown = MonkAction.isOpoOpoForm(player);
            MonkAction.giveRaptorForm(player);

            // TODO calculate damage with 100 (flank 140) potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Dragon Kick"),
                    140,
                    targets,
                    false
            );

            for (Entity target : targets) {
                if(inflictBluntDown) {
                    if (target instanceof LivingEntity living) {
                        living.addStatusEffect(new StatusEffectInstance(
                                BluntResistanceDownStatusEffect.INSTANCE.reference(),
                                15 * 20
                        ), player);
                    }
                }
            }
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    TWIN_SNAKES(
            0,
            3,
            0,
            Identifier.of(Alymod.ID, "action/twin_snakes")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isRaptorForm(player) || MonkAction.isPerfectBalance(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isRaptorForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!enabled(player, tickDelta)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.giveCoeurlForm(player);

            // TODO calculate damage with 100 (flank 130) potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Twin Snakes"),
                    130,
                    targets,
                    false
            );

            player.addStatusEffect(new StatusEffectInstance(
                    TwinSnakesStatusEffect.INSTANCE.reference(),
                    15 * 20
            ), player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    DEMOLISH(
            0,
            3,
            0,
            Identifier.of(Alymod.ID, "action/demolish")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isCoeurlForm(player) || MonkAction.isPerfectBalance(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isCoeurlForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!enabled(player, tickDelta)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.giveOpoOpoForm(player);

            // TODO calculate damage with 30 (rear 70) potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Demolish"),
                    70,
                    targets,
                    false
            );

            for (Entity target : targets) {
                if (target instanceof LivingEntity living) {
                    living.addStatusEffect(new StatusEffectInstance(
                            DemolishStatusEffect.INSTANCE.reference(),
                            18 * 20
                    ), player);
                }
            }
            MonkAction.giveGlStack(player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    BOOTSHINE(
            0,
            3,
            0,
            Identifier.of(Alymod.ID, "action/bootshine")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isOpoOpoForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.giveRaptorForm(player);

            // TODO calculate damage with 140 (rear crit) potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Bootshine"),
                    140,
                    targets,
                    true
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    TRUE_STRIKE(
            0,
            3,
            0,
            Identifier.of(Alymod.ID, "action/true_strike")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isRaptorForm(player) || MonkAction.isPerfectBalance(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isRaptorForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!enabled(player, tickDelta)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.giveCoeurlForm(player);

            // TODO calculate damage with 140 (rear 180) potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("True Strike"),
                    180,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    SNAP_PUNCH(
            0,
            3,
            0,
            Identifier.of(Alymod.ID, "action/snap_punch")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isCoeurlForm(player) || MonkAction.isPerfectBalance(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isCoeurlForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!enabled(player, tickDelta)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.giveOpoOpoForm(player);

            // TODO calculate damage with 130 (flank 170) potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Snap Punch"),
                    170,
                    targets,
                    false
            );

            MonkAction.giveGlStack(player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    ARM_OF_THE_DESTROYER(
            0,
            0,
            5,
            Identifier.of(Alymod.ID,  "action/arm_of_the_destroyer")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isOpoOpoForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            List<Entity> targets = player.getWorld()
                    .getOtherEntities(
                            player,
                            player.getBoundingBox().expand(this.getRadius()),
                            EntityPredicates.EXCEPT_SPECTATOR.and(e ->
                                    player.squaredDistanceTo(e) < (this.getRadius() * this.getRadius())));
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    targets.stream().map(Entity::getId).toList()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            if(!targets.isEmpty()) {
                MonkAction.giveRaptorForm(player);

                // TODO do damage (50 potency) to targets
                MonkAction.temporaryDamageHelper(
                        player,
                        Text.literal("Arm of the Destroyer"),
                        50,
                        targets,
                        false
                );
            }
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    ONE_ILM_PUNCH(
            0,
            3,
            0,
            Identifier.of(Alymod.ID, "action/one_ilm_punch")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isRaptorForm(player) || MonkAction.isPerfectBalance(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isRaptorForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!enabled(player, tickDelta)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            // TODO do 120 damage to targets, maybe stun?
            MonkAction.giveCoeurlForm(player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    ROCKBREAKER(
            0,
            5,
            5,
            Identifier.of(Alymod.ID, "action/rockbreaker")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isCoeurlForm(player) || MonkAction.isPerfectBalance(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return MonkAction.isCoeurlForm(player);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!enabled(player, tickDelta)) return false;

            List<Entity> targets = player.getWorld()
                    .getOtherEntities(
                            player,
                            player.getBoundingBox().expand(this.getRadius()),
                            EntityPredicates.EXCEPT_SPECTATOR.and(e ->
                                    ConeBoxIntersection.intersects(
                                            e.getBoundingBox(),
                                            player.getEyePos(),
                                            player.getRotationVecClient(),
                                            1 + this.getRange(),
                                            Math.PI / 4.0d
                                    ))
                    );

            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    targets.stream().map(Entity::getId).toList()
            ));

            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            if(!targets.isEmpty()) {
                MonkAction.giveOpoOpoForm(player);

                // TODO damage calc (130)
                MonkAction.temporaryDamageHelper(
                        player,
                        Text.literal("Rockbreaker"),
                        130,
                        targets,
                        false
                );

                MonkAction.giveGlStack(player);
            }
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    FORM_SHIFT(
            0,
            0,
            0,
            Identifier.of(Alymod.ID, "action/form_shift")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    List.of()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            if(MonkAction.isOpoOpoForm(player)) {
                MonkAction.giveRaptorForm(player);
            } else if (MonkAction.isRaptorForm(player)) {
                MonkAction.giveCoeurlForm(player);
            } else {
                MonkAction.giveOpoOpoForm(player);
            }
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return MonkAction.weaponskillDuration(player);
        }
    },
    INTERNAL_RELEASE(
            1,
            0,
            0,
            Identifier.of(Alymod.ID, "action/internal_release")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    List.of()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            player.addStatusEffect(new StatusEffectInstance(
                    InternalReleaseStatusEffect.INSTANCE.reference(),
                    15 * 20
            ), player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(60);
        }
    },
    FISTS_OF_EARTH(
            2,
            0,
            0,
            Identifier.of(Alymod.ID, "action/fists_of_earth")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    List.of()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            if(MonkAction.isFistsOfEarth(player)) {
                player.removeStatusEffect(FistsOfEarthStatusEffect.INSTANCE.reference());
            } else {
                MonkAction.giveFistsOfEarth(player);
            }
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(3);
        }
    },
    SECOND_WIND(
            3,
            0,
            0,
            Identifier.of(Alymod.ID, "action/second_wind")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            // TODO heal (& ask about scaling with attack power)
            return false;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {

        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(120);
        }
    },
    MANTRA(
            4,
            0,
            7,
            Identifier.of(Alymod.ID, "action/mantra")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            // TODO buff players in radius
            return false;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {

        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(90);
        }
    },
    MEDITATION(
            0,
            0,
            0,
            Identifier.of(Alymod.ID, "action/meditation")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            int chakra = MonkAction.getChakra(player);
            return chakra < 5;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            int chakra = MonkAction.getChakra(player);
            if(chakra < 5) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of()
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.giveChakra(player);
        }

        @Override
        public MonkAction replacement(PlayerEntity player) {
            int chakra = MonkAction.getChakra(player);
            if(chakra < 5) return null;
            else return MonkAction.THE_FORBIDDEN_CHAKRA;
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofMillis(1200);
        }
    },
    TORNADO_KICK(
            5,
            3,
            0,
            Identifier.of(Alymod.ID, "action/tornado_kick")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            int gl = MonkAction.getGlStacks(player);
            return !(gl < 3);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            int gl = MonkAction.getGlStacks(player);
            if(gl < 3) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.consumeGlStacks(player);

            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Tornado Kick"),
                    330,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(10);
        }
    },
    RIDDLE_OF_EARTH(
            6,
            0,
            0,
            Identifier.of(Alymod.ID, "action/riddle_of_earth")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            // TODO give buff and extend GL
            return false;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {

        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(60);
        }
    },
    RIDDLE_OF_FIRE(
            7,
            0,
            0,
            Identifier.of(Alymod.ID, "action/riddle_of_fire")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    List.of()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.giveFistsOfFire(player);
            player.addStatusEffect(new StatusEffectInstance(
                    RiddleOfFireStatusEffect.INSTANCE.reference(),
                    20 * 20
            ), player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(90);
        }
    },
    BROTHERHOOD(
            8,
            0,
            15,
            Identifier.of(Alymod.ID, "action/brotherhood")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            // TODO give BH buffs to self and surrounding players
            return false;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {

        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(90);
        }
    },
    STEEL_PEAK(
            9,
            3,
            0,
            Identifier.of(Alymod.ID, "action/steel_peak")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));
                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            // TODO calculate damage with 150 potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Steel Peak"),
                    150,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(40);
        }
    },
    SHOULDER_TACKLE(
            10,
            20,
            0,
            Identifier.of(Alymod.ID, "action/shoulder_tackle")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));

                Vec3d ray = player.getRotationVec(tickDelta);
                double rayLength = ConeBoxIntersection.lineSegmentIntersectsBox(
                        target.getBoundingBox().offset(player.getEyePos().negate()),
                        ray,
                        Double.POSITIVE_INFINITY
                );

                player.move(MovementType.SELF, ray.multiply(Math.max(0, rayLength - 1.5d)));

                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Shoulder Tackle"),
                    100,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(30);
        }

        @Override
        public MonkAction replacement(PlayerEntity player) {
            if(MonkAction.isFistsOfFire(player)) {
                return MonkAction.FIRE_TACKLE;
            }
            if(MonkAction.isFistsOfWind(player)) {
                return MonkAction.WIND_TACKLE;
            }
            if(MonkAction.isFistsOfEarth(player)) {
                return MonkAction.EARTH_TACKLE;
            }
            return null;
        }
    },
    HOWLING_FIST(
            11,
            10,
            10,
            Identifier.of(Alymod.ID, "action/howling_fist")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            // TODO damage
            return false;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {

        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(60);
        }
    },
    ELIXIR_FIELD(
            12,
            0,
            5,
            Identifier.of(Alymod.ID, "action/elixir_field")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            List<Entity> targets = player.getWorld()
                    .getOtherEntities(
                            player,
                            player.getBoundingBox().expand(this.getRadius()),
                            EntityPredicates.EXCEPT_SPECTATOR.and(e ->
                                    player.squaredDistanceTo(e) < (this.getRadius() * this.getRadius())));
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    targets.stream().map(Entity::getId).toList()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            // TODO do damage of 220 potency
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Elixir Field"),
                    220,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(30);
        }
    },
    PERFECT_BALANCE(
            13,
            0,
            0,
            Identifier.of(Alymod.ID, "action/perfect_balance")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    List.of()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.givePerfectBalance(player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(60);
        }
    },
    FISTS_OF_WIND(
            2,
            0,
            0,
            Identifier.of(Alymod.ID, "action/fists_of_wind")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    List.of()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            if(MonkAction.isFistsOfWind(player)) {
                player.removeStatusEffect(FistsOfWindStatusEffect.INSTANCE.reference());
            } else {
                MonkAction.giveFistsOfWind(player);
            }
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(3);
        }
    },
    FISTS_OF_FIRE(
            2,
            0,
            0,
            Identifier.of(Alymod.ID, "action/fists_of_fire")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return true;
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            ClientPlayNetworking.send(new MonkActionUsePayload(
                    this.ordinal(),
                    List.of()
            ));
            return true;
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            if(MonkAction.isFistsOfFire(player)) {
                player.removeStatusEffect(FistsOfFireStatusEffect.INSTANCE.reference());
            } else {
                MonkAction.giveFistsOfFire(player);
            }
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(3);
        }
    },
    THE_FORBIDDEN_CHAKRA(
            14,
            3,
            0,
            Identifier.of(Alymod.ID, "action/the_forbidden_chakra")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            int chakra = MonkAction.getChakra(player);
            return !(chakra < 5);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            int chakra = MonkAction.getChakra(player);
            if(chakra < 5) return false;
            else {
                Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
                if(target != null) {
                    ClientPlayNetworking.send(new MonkActionUsePayload(
                            this.ordinal(),
                            List.of(target.getId())
                    ));
                    return true;
                } else {
                    return false;
                }
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            MonkAction.consumeChakra(player);

            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("The Forbidden Chakra"),
                    250,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(5);
        }
    },
    FIRE_TACKLE(
            10,
            20,
            0,
            Identifier.of(Alymod.ID, "action/fire_tackle")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isFistsOfFire(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!MonkAction.isFistsOfFire(player)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));

                Vec3d ray = player.getRotationVec(tickDelta);
                double rayLength = ConeBoxIntersection.lineSegmentIntersectsBox(
                        target.getBoundingBox().offset(player.getEyePos().negate()),
                        ray,
                        Double.POSITIVE_INFINITY
                );

                player.move(MovementType.SELF, ray.multiply(Math.max(0, rayLength - 1.5d)));

                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            // TODO 130 pot
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Fire Tackle"),
                    130,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(30);
        }
    },
    WIND_TACKLE(
            10,
            20,
            0,
            Identifier.of(Alymod.ID, "action/wind_tackle")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isFistsOfWind(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!MonkAction.isFistsOfWind(player)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));

                Vec3d ray = player.getRotationVec(tickDelta);
                double rayLength = ConeBoxIntersection.lineSegmentIntersectsBox(
                        target.getBoundingBox().offset(player.getEyePos().negate()),
                        ray,
                        Double.POSITIVE_INFINITY
                );

                player.move(MovementType.SELF, ray.multiply(Math.max(0, rayLength - 1.5d)));

                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            // TODO 65 pot
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Wind Tackle"),
                    65,
                    targets,
                    false
            );

            player.addStatusEffect(new StatusEffectInstance(
                    RiddleOfWindStatusEffect.INSTANCE.reference(),
                    10 * 20
            ), player);
        }

        @Override
        public MonkAction replacement(PlayerEntity player) {
            if(player.hasStatusEffect(RiddleOfWindStatusEffect.INSTANCE.reference())) {
                return MonkAction.RIDDLE_OF_WIND;
            }
            return null;
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(30);
        }
    },
    EARTH_TACKLE(
            10,
            20,
            0,
            Identifier.of(Alymod.ID, "action/earth_tackle")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isFistsOfEarth(player);
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return false;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!MonkAction.isFistsOfEarth(player)) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));

                Vec3d ray = player.getRotationVec(tickDelta);
                double rayLength = ConeBoxIntersection.lineSegmentIntersectsBox(
                        target.getBoundingBox().offset(player.getEyePos().negate()),
                        ray,
                        Double.POSITIVE_INFINITY
                );

                player.move(MovementType.SELF, ray.multiply(Math.max(0, rayLength - 1.5d)));

                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            // TODO 100 pot, kb
            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Earth Tackle"),
                    100,
                    targets,
                    false
            );
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofSeconds(30);
        }
    },
    RIDDLE_OF_WIND(
            15,
            20,
            0,
            Identifier.of(Alymod.ID, "action/riddle_of_wind")
    ) {
        @Override
        public boolean enabled(PlayerEntity player, float tickDelta) {
            return MonkAction.isFistsOfWind(player) &&
                    player.hasStatusEffect(RiddleOfWindStatusEffect.INSTANCE.reference());
        }

        @Override
        public boolean highlighted(PlayerEntity player, float tickDelta) {
            return player.hasStatusEffect(RiddleOfWindStatusEffect.INSTANCE.reference());
        }

        @Environment(EnvType.CLIENT)
        @Override
        public boolean onCast(ClientPlayerEntity player, float tickDelta) {
            if(!MonkAction.isFistsOfWind(player) ||
                !player.hasStatusEffect(RiddleOfWindStatusEffect.INSTANCE.reference())) return false;

            Entity target = MonkAction.getTarget(player, this.getRange(), tickDelta);
            if(target != null) {
                ClientPlayNetworking.send(new MonkActionUsePayload(
                        this.ordinal(),
                        List.of(target.getId())
                ));

                Vec3d ray = player.getRotationVec(tickDelta);
                double rayLength = ConeBoxIntersection.lineSegmentIntersectsBox(
                        target.getBoundingBox().offset(player.getEyePos().negate()),
                        ray,
                        Double.POSITIVE_INFINITY
                );

                player.move(MovementType.SELF, ray.multiply(Math.max(0, rayLength - 1.5d)));

                return true;
            } else {
                return false;
            }
        }

        @Environment(EnvType.SERVER)
        @Override
        public void onCastServer(ServerPlayerEntity player, List<Entity> targets) {
            player.removeStatusEffect(RiddleOfWindStatusEffect.INSTANCE.reference());

            MonkAction.temporaryDamageHelper(
                    player,
                    Text.literal("Riddle of Wind"),
                    65,
                    targets,
                    false
            );

            MonkAction.giveGlStack(player);
        }

        @Override
        public Duration getRecast(PlayerEntity player) {
            return Duration.ofMillis(500);
        }
    };

    public static int rawWd() { return 93; }
    public static int rawStr() { return 1388; }
    public static int rawDh() { return 1088; }
    public static int rawCrit() { return 1241; }
    public static int rawDet() { return 701; }
    public static int rawSks() { return 568; }

    public static int lv70Det() { return 292; }
    public static int lv70Sub() { return 364; }
    public static int lv70Div() { return 900; }
    public static int apDiv() { return 130; }

    public static int mnkMod() { return 110; }

    public static double probDh() {
        return Math.floorDiv(550 * (rawDh() - lv70Sub()), lv70Div()) / 1000.0d;
    }

    public static double probCrit() {
        return (50 + Math.floorDiv(200 * (rawCrit() - lv70Sub()), lv70Div())) / 1000.0d;
    }

    public static int funcWd() {
        return rawWd() + Math.floorDiv(lv70Det() * mnkMod(), 1000);
    }

    public static double funcAp() {
        return 1.0 + (Math.floorDiv(apDiv() * (rawStr() - lv70Det()), lv70Det()) / 100.0d);
    }

    public static double funcDet() {
        return 1.0 + (Math.floorDiv(130 * (rawDet() - lv70Det()), lv70Div()) / 1000.0d);
    }

    public static double funcCrit() {
        return 1.0 + ((400.0d + Math.floorDiv(200 * (rawCrit() - lv70Sub()), lv70Div())) / 1000.0d);
    }

    public static double funcSpeed() {
        return 1.0 - (Math.floorDiv(130 * (rawSks() - lv70Sub()), lv70Div()) / 1000.0d);
    }

    public static boolean willCrit(PlayerEntity player, Random r) {
        double internalRelease =
                player.hasStatusEffect(InternalReleaseStatusEffect.INSTANCE.reference()) ? 0.3 : 0.0;
        return r.nextDouble() <= (internalRelease + probCrit());
    }

    public static boolean willDh(Random r) {
        return r.nextDouble() <= probDh();
    }

    public static int directDamage(int potency, Random r, boolean didCrit, boolean didDh, double buffs) {
        int d1 = (int) Math.floor(potency * funcAp() * funcDet());
        int d2 = Math.floorDiv(d1 * funcWd(), 100);

        double critMulti = didCrit ? funcCrit() : 1.0d;
        double dhMulti = didDh ? 1.25d : 1.0d;

        int d3 = (int) Math.floor(d2 * critMulti * dhMulti);

        double variance = 0.95d + (0.1d * r.nextDouble());
        int d4 = (int) Math.floor(d3 * variance * buffs);

        return d4;
    }

    public static double calculateDamageBuffs(PlayerEntity player) {
        double glBonus =
                (100 + (10 * MonkAction.getGlStacks(player))) / 100.0d;
        double twinSnakesBonus =
                player.hasStatusEffect(TwinSnakesStatusEffect.INSTANCE.reference()) ? 1.10 : 1.00;
        double rofBonus =
                player.hasStatusEffect(RiddleOfFireStatusEffect.INSTANCE.reference()) ? 1.30 : 1.00;
        double fofBonus =
                player.hasStatusEffect(FistsOfFireStatusEffect.INSTANCE.reference()) ? 1.06 : 1.00;

        return glBonus * twinSnakesBonus * rofBonus * fofBonus;
    }

    public static boolean isOpoOpoForm(PlayerEntity player) {
        return player.hasStatusEffect(OpoOpoFormStatusEffect.INSTANCE.reference());
    }

    public static boolean isRaptorForm(PlayerEntity player) {
        return player.hasStatusEffect(RaptorFormStatusEffect.INSTANCE.reference());
    }

    public static boolean isCoeurlForm(PlayerEntity player) {
        return player.hasStatusEffect(CoeurlFormStatusEffect.INSTANCE.reference());
    }

    public static void giveOpoOpoForm(PlayerEntity player) {
        if(MonkAction.isPerfectBalance(player)) return;

        player.removeStatusEffect(CoeurlFormStatusEffect.INSTANCE.reference());
        player.addStatusEffect(
                new StatusEffectInstance(OpoOpoFormStatusEffect.INSTANCE.reference(), 10 * 20),
                player
        );
    }

    public static void giveRaptorForm(PlayerEntity player) {
        if(MonkAction.isPerfectBalance(player)) return;

        player.removeStatusEffect(OpoOpoFormStatusEffect.INSTANCE.reference());
        player.removeStatusEffect(CoeurlFormStatusEffect.INSTANCE.reference());
        player.addStatusEffect(
                new StatusEffectInstance(RaptorFormStatusEffect.INSTANCE.reference(), 10 * 20),
                player
        );
    }

    public static void giveCoeurlForm(PlayerEntity player) {
        if(MonkAction.isPerfectBalance(player)) return;

        player.removeStatusEffect(RaptorFormStatusEffect.INSTANCE.reference());
        player.addStatusEffect(
                new StatusEffectInstance(CoeurlFormStatusEffect.INSTANCE.reference(), 10 * 20),
                player
        );
    }

    public static boolean isPerfectBalance(PlayerEntity player) {
        return player.hasStatusEffect(PerfectBalanceStatusEffect.INSTANCE.reference());
    }

    public static void givePerfectBalance(PlayerEntity player) {
        player.removeStatusEffect(OpoOpoFormStatusEffect.INSTANCE.reference());
        player.removeStatusEffect(RaptorFormStatusEffect.INSTANCE.reference());
        player.removeStatusEffect(CoeurlFormStatusEffect.INSTANCE.reference());
        player.addStatusEffect(
                new StatusEffectInstance(PerfectBalanceStatusEffect.INSTANCE.reference(), 10 * 20),
                player
        );
    }

    public static boolean isFistsOfFire(PlayerEntity player) {
        return player.hasStatusEffect(FistsOfFireStatusEffect.INSTANCE.reference());
    }

    public static void giveFistsOfFire(PlayerEntity player) {
        player.removeStatusEffect(FistsOfWindStatusEffect.INSTANCE.reference());
        player.removeStatusEffect(FistsOfEarthStatusEffect.INSTANCE.reference());
        player.addStatusEffect(
                new StatusEffectInstance(FistsOfFireStatusEffect.INSTANCE.reference(), StatusEffectInstance.INFINITE),
                player
        );
    }

    public static boolean isFistsOfWind(PlayerEntity player) {
        return player.hasStatusEffect(FistsOfWindStatusEffect.INSTANCE.reference());
    }

    public static void giveFistsOfWind(PlayerEntity player) {
        player.removeStatusEffect(FistsOfFireStatusEffect.INSTANCE.reference());
        player.removeStatusEffect(FistsOfEarthStatusEffect.INSTANCE.reference());
        player.addStatusEffect(
                new StatusEffectInstance(FistsOfWindStatusEffect.INSTANCE.reference(), StatusEffectInstance.INFINITE),
                player
        );
    }

    public static boolean isFistsOfEarth(PlayerEntity player) {
        return player.hasStatusEffect(FistsOfEarthStatusEffect.INSTANCE.reference());
    }

    public static void giveFistsOfEarth(PlayerEntity player) {
        player.removeStatusEffect(FistsOfFireStatusEffect.INSTANCE.reference());
        player.removeStatusEffect(FistsOfWindStatusEffect.INSTANCE.reference());
        player.addStatusEffect(
                new StatusEffectInstance(FistsOfEarthStatusEffect.INSTANCE.reference(), StatusEffectInstance.INFINITE),
                player
        );
    }

    public static int getGlStacks(PlayerEntity player) {
        long t = player.getWorld().getTime();
        if(player.hasAttached(PlayerMonkAttachments.greasedLightningExpires())) {
            long expires = player.getAttached(PlayerMonkAttachments.greasedLightningExpires());
            if(expires <= t) {
                player.setAttached(PlayerMonkAttachments.greasedLightning(), 0);
                return 0;
            } else {
                return player.getAttached(PlayerMonkAttachments.greasedLightning());
            }
        } else {
            player.setAttached(PlayerMonkAttachments.greasedLightning(), 0);
            return 0;
        }
    }

    public static void giveGlStack(ServerPlayerEntity player) {
        int gl = MonkAction.getGlStacks(player);
        if(gl < 3) {
            player.setAttached(PlayerMonkAttachments.greasedLightning(), gl + 1);
        }
        long now = player.getWorld().getTime();
        long expires = now + (16L * 20L);
        player.setAttached(PlayerMonkAttachments.greasedLightningExpires(), expires);
        MonkAction.syncGaugeState(player);
    }

    public static void consumeGlStacks(ServerPlayerEntity player) {
        player.setAttached(PlayerMonkAttachments.greasedLightning(), 0);
        player.setAttached(PlayerMonkAttachments.greasedLightningExpires(), 0L);
        MonkAction.syncGaugeState(player);
    }

    public static int getChakra(PlayerEntity player) {
        return player.getAttachedOrCreate(PlayerMonkAttachments.chakra());
    }

    public static void giveChakra(ServerPlayerEntity player) {
        int chakra = MonkAction.getChakra(player);
        if(chakra < 5) {
            player.setAttached(PlayerMonkAttachments.chakra(), chakra + 1);
        }
        MonkAction.syncGaugeState(player);
    }

    public static void consumeChakra(ServerPlayerEntity player) {
        player.setAttached(PlayerMonkAttachments.chakra(), 0);
        MonkAction.syncGaugeState(player);
    }

    public static void syncGaugeState(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new MonkAttachmentUpdatePayload(
                MonkAction.getGlStacks(player),
                player.getAttachedOrCreate(PlayerMonkAttachments.greasedLightningExpires(), () -> 0L),
                MonkAction.getChakra(player)
        ));
    }

    public static void temporaryDamageHelper(ServerPlayerEntity player, Text action, int potency, List<Entity> targets, boolean guaranteedCrit) {
        boolean anyCrit = false;

        for(Entity target : targets) {
            boolean crit = guaranteedCrit || MonkAction.willCrit(player, player.getRandom());
            boolean dh = MonkAction.willDh(player.getRandom());
            int damage = MonkAction.directDamage(
                    potency,
                    player.getRandom(),
                    crit,
                    dh,
                    MonkAction.calculateDamageBuffs(player)
            );

            // puts GL3 Twin DK dps at 1/4th of sharp V netherite sword
            // this is to balance out double tk opener
            target.damage(player.getServerWorld().getDamageSources().playerAttack(player), damage / 200.0F);

            MutableText message = Text.literal("Hit ");
            message.append(target.getDisplayName());
            message.append(" with ");
            message.append(action);
            message.append(" for ");

            MutableText damageText = Text.literal(Integer.toString(damage));
            if(crit) {
                damageText.append("!");
                if(dh) {
                    damageText.append("!!");
                    damageText.styled(s -> s.withBold(true));
                }
                damageText.styled(s -> s.withColor(0xFF0000));
            } else if(dh) {
                damageText.styled(s -> s.withColor(0xFFFF00));
            }

            message.append(damageText);

            player.sendMessage(message);

            anyCrit |= crit;
        }

        if(anyCrit) {
            if(player.getRandom().nextDouble() < 0.50) {
                MonkAction.giveChakra(player);
            }
        }
    }

    public static Duration weaponskillDuration(PlayerEntity player) {
        // 2500 ms
        int gcd1 = (int) Math.floor(2500 * funcSpeed());

        int gl = MonkAction.getGlStacks(player);
        int glBonus = 100 - (gl * 5);
        // gcd2 in 1000ths of a second
        int gcd2 = Math.floorDiv(gcd1 * glBonus, 100);

        boolean rof = player.hasStatusEffect(RiddleOfFireStatusEffect.INSTANCE.reference());
        int rofBonus = rof ? 115 : 100;
        // gcd3 in 100ths of a second
        int gcd3 = Math.floorDiv(gcd2 * rofBonus, 1000);

        return Duration.ofMillis(10L * gcd3);
    }

    public static Entity getTarget(PlayerEntity player, double radius, float tickDelta) {
        Vec3d segment = player.getRotationVec(tickDelta).multiply(radius);
        Vec3d from = player.getCameraPosVec(tickDelta);
        Vec3d to = from.add(segment);
        Box box = player.getBoundingBox().stretch(segment).expand(1.0);

        EntityHitResult res = ProjectileUtil.raycast(
                player,
                from,
                to,
                box,
                EntityPredicates.EXCEPT_SPECTATOR.and(Entity::canHit),
                radius * radius // of course
        );
        if(res != null) {
            return res.getEntity();
        } else return null;
    }

    private final int cooldownGroup;
    private final int range;
    private final int radius;
    private final Identifier guiTexture;

    MonkAction(int cooldownGroup, int range, int radius, Identifier guiTexture) {
        this.cooldownGroup = cooldownGroup;
        this.range = range;
        this.radius = radius;
        this.guiTexture = guiTexture;
    }

    public abstract boolean enabled(PlayerEntity player, float tickDelta);

    public abstract boolean highlighted(PlayerEntity player, float tickDelta);

    @Environment(EnvType.CLIENT)
    public abstract boolean onCast(ClientPlayerEntity player, float tickDelta);

    @Environment(EnvType.SERVER)
    public abstract void onCastServer(ServerPlayerEntity player, List<Entity> targets);

    public abstract Duration getRecast(PlayerEntity player);

    public MonkAction replacement(PlayerEntity player) {
        return null;
    }

    public int getCooldownGroup() {
        return cooldownGroup;
    }

    public int getRange() {
        return range;
    }

    public int getRadius() {
        return radius;
    }

    public Identifier getGuiTexture() {
        return guiTexture;
    }
}
