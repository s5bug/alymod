package tf.bug.alymod.monk;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import tf.bug.alymod.attachment.PlayerMonkAttachments;
import tf.bug.alymod.effect.*;
import tf.bug.alymod.network.MonkAttachmentUpdatePayload;

public final class StatusHelpers {
    private StatusHelpers() {}

    public static boolean hasPerfectBalance(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.PERFECT_BALANCE.reference());
    }

    public static void givePerfectBalance(PlayerEntity pe) {
        pe.removeStatusEffect(MonkStatusEffects.OPO_OPO_FORM.reference());
        pe.removeStatusEffect(MonkStatusEffects.RAPTOR_FORM.reference());
        pe.removeStatusEffect(MonkStatusEffects.COEURL_FORM.reference());

        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.PERFECT_BALANCE.reference(),
                10 * 20
        ), pe);
    }

    public static boolean isOpoOpoForm(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.OPO_OPO_FORM.reference());
    }

    public static boolean hasOpoOpoBonus(PlayerEntity pe) {
        return isOpoOpoForm(pe) || hasPerfectBalance(pe);
    }

    public static void giveOpoOpoForm(PlayerEntity pe) {
        if(StatusHelpers.hasPerfectBalance(pe)) return;

        pe.removeStatusEffect(MonkStatusEffects.COEURL_FORM.reference());
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.OPO_OPO_FORM.reference(),
                10 * 20
        ), pe);
    }

    public static boolean isRaptorForm(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.RAPTOR_FORM.reference());
    }

    public static boolean hasRaptorBonus(PlayerEntity pe) {
        return isRaptorForm(pe) || hasPerfectBalance(pe);
    }

    public static void giveRaptorForm(PlayerEntity pe) {
        if(StatusHelpers.hasPerfectBalance(pe)) return;

        pe.removeStatusEffect(MonkStatusEffects.OPO_OPO_FORM.reference());
        pe.removeStatusEffect(MonkStatusEffects.COEURL_FORM.reference());
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.RAPTOR_FORM.reference(),
                10 * 20
        ), pe);
    }

    public static boolean isCoeurlForm(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.COEURL_FORM.reference());
    }

    public static boolean hasCoeurlBonus(PlayerEntity pe) {
        return isCoeurlForm(pe) || hasPerfectBalance(pe);
    }

    public static void giveCoeurlForm(PlayerEntity pe) {
        if(StatusHelpers.hasPerfectBalance(pe)) return;

        pe.removeStatusEffect(MonkStatusEffects.RAPTOR_FORM.reference());
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.COEURL_FORM.reference(),
                10 * 20
        ), pe);
    }

    public static boolean isFistsOfEarth(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.FISTS_OF_EARTH.reference());
    }

    public static void toggleFistsOfEarth(PlayerEntity pe) {
        if(StatusHelpers.isFistsOfEarth(pe)) {
            pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_EARTH.reference());
        } else {
            pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_WIND.reference());
            pe.removeStatusEffect(MonkStatusEffects.RIDDLE_OF_WIND.reference());
            pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_FIRE.reference());
            pe.removeStatusEffect(MonkStatusEffects.RIDDLE_OF_FIRE.reference());

            pe.addStatusEffect(new StatusEffectInstance(
                    MonkStatusEffects.FISTS_OF_EARTH.reference(),
                    StatusEffectInstance.INFINITE
            ), pe);
        }
    }

    public static boolean isFistsOfWind(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.FISTS_OF_WIND.reference());
    }

    public static void toggleFistsOfWind(PlayerEntity pe) {
        if(StatusHelpers.isFistsOfWind(pe)) {
            pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_WIND.reference());
        } else {
            pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_EARTH.reference());
            pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_FIRE.reference());
            pe.removeStatusEffect(MonkStatusEffects.RIDDLE_OF_FIRE.reference());

            pe.addStatusEffect(new StatusEffectInstance(
                    MonkStatusEffects.FISTS_OF_WIND.reference(),
                    StatusEffectInstance.INFINITE
            ), pe);
        }
    }

    public static boolean isFistsOfFire(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.FISTS_OF_FIRE.reference());
    }

    public static void giveFistsOfFire(PlayerEntity pe) {
        pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_EARTH.reference());
        pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_WIND.reference());
        pe.removeStatusEffect(MonkStatusEffects.RIDDLE_OF_WIND.reference());

        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.FISTS_OF_FIRE.reference(),
                StatusEffectInstance.INFINITE
        ), pe);
    }

    public static void toggleFistsOfFire(PlayerEntity pe) {
        if(StatusHelpers.isFistsOfFire(pe)) {
            pe.removeStatusEffect(MonkStatusEffects.FISTS_OF_FIRE.reference());
            pe.removeStatusEffect(MonkStatusEffects.RIDDLE_OF_FIRE.reference());
        } else {
            StatusHelpers.giveFistsOfFire(pe);
        }
    }

    public static void giveInternalRelease(PlayerEntity pe) {
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.INTERNAL_RELEASE.reference(),
                15 * 20
        ), pe);
    }

    public static void giveTwinSnakes(PlayerEntity pe) {
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.TWIN_SNAKES.reference(),
                15 * 20
        ), pe);
    }

    public static boolean hasRiddleOfWind(PlayerEntity pe) {
        return pe.hasStatusEffect(MonkStatusEffects.RIDDLE_OF_WIND.reference());
    }

    public static void giveRiddleOfWind(PlayerEntity pe) {
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.RIDDLE_OF_WIND.reference(),
                10 * 20
        ));
    }

    public static void consumeRiddleOfWind(PlayerEntity pe) {
        pe.removeStatusEffect(MonkStatusEffects.RIDDLE_OF_WIND.reference());
    }

    public static void giveRiddleOfFire(PlayerEntity pe) {
        StatusHelpers.giveFistsOfFire(pe);
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.RIDDLE_OF_FIRE.reference(),
                (20 * 20) + 16 // 20.8s
        ), pe);
    }

    public static void giveBrotherhood(PlayerEntity pe) {
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.BROTHERHOOD.reference(),
                15 * 20
        ), pe);
    }

    public static void giveMeditativeBrotherhood(PlayerEntity pe) {
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.MEDITATIVE_BROTHERHOOD.reference(),
                15 * 20
        ), pe);
    }

    public static void syncGaugeState(ServerPlayerEntity pe) {
        ServerPlayNetworking.send(pe, new MonkAttachmentUpdatePayload(
                StatusHelpers.getGreasedLightning(pe),
                pe.getAttachedOrCreate(PlayerMonkAttachments.greasedLightningExpires(), () -> 0L),
                StatusHelpers.getChakra(pe)
        ));
    }

    public static int getGreasedLightning(PlayerEntity pe) {
        long t = pe.getWorld().getTime();
        if(pe.hasAttached(PlayerMonkAttachments.greasedLightningExpires())) {
            long expires = pe.getAttached(PlayerMonkAttachments.greasedLightningExpires());
            if(expires <= t) {
                pe.setAttached(PlayerMonkAttachments.greasedLightning(), 0);
                return 0;
            } else {
                return pe.getAttached(PlayerMonkAttachments.greasedLightning());
            }
        } else {
            pe.setAttached(PlayerMonkAttachments.greasedLightning(), 0);
            return 0;
        }
    }

    public static void giveGreasedLightning(ServerPlayerEntity pe) {
        int gl = StatusHelpers.getGreasedLightning(pe);
        if(gl < 3) {
            pe.setAttached(PlayerMonkAttachments.greasedLightning(), gl + 1);
        }
        long now = pe.getWorld().getTime();
        long expires = now + (16L * 20L);
        pe.setAttached(PlayerMonkAttachments.greasedLightningExpires(), expires);
        StatusHelpers.syncGaugeState(pe);
    }

    public static void consumeGreasedLightning(ServerPlayerEntity pe) {
        pe.setAttached(PlayerMonkAttachments.greasedLightning(), 0);
        pe.setAttached(PlayerMonkAttachments.greasedLightningExpires(), 0L);
        StatusHelpers.syncGaugeState(pe);
    }

    public static int getChakra(PlayerEntity pe) {
        return pe.getAttachedOrCreate(PlayerMonkAttachments.chakra());
    }

    public static void giveChakra(ServerPlayerEntity pe) {
        int chakra = StatusHelpers.getChakra(pe);
        if(chakra < 5) {
            pe.setAttached(PlayerMonkAttachments.chakra(), chakra + 1);
        }
        StatusHelpers.syncGaugeState(pe);
    }

    public static void consumeChakra(ServerPlayerEntity pe) {
        pe.setAttached(PlayerMonkAttachments.chakra(), 0);
        StatusHelpers.syncGaugeState(pe);
    }

    public static void giveSprint(PlayerEntity pe, int durationTicks) {
        pe.addStatusEffect(new StatusEffectInstance(
                MonkStatusEffects.SPRINT.reference(),
                durationTicks
        ), pe);
    }

}
