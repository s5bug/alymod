package tf.bug.alymod.monk;

import java.time.Duration;
import net.minecraft.client.network.ClientPlayerEntity;
import tf.bug.alymod.effect.MonkStatusEffects;

public interface CooldownStrategy {

    Duration getCooldown(ClientPlayerEntity player, MonkStats stats);

    public static final record Weaponskill(Duration baseRecast) implements CooldownStrategy {
        public Weaponskill() { this(Duration.ofMillis(2500)); }

        @Override
        public Duration getCooldown(ClientPlayerEntity player, MonkStats stats) {
            int gcd1 = (int) Math.floor(baseRecast.toMillis() * stats.getMultiplierSkillSpeed());

            int gl = StatusHelpers.getGreasedLightning(player);
            int glBonus = 100 - (gl * 5);
            // gcd2 in 1000ths of a second
            int gcd2 = Math.floorDiv(gcd1 * glBonus, 100);

            boolean rof = player.hasStatusEffect(MonkStatusEffects.RIDDLE_OF_FIRE.reference());
            int rofBonus = rof ? 115 : 100;
            // gcd3 in 100ths of a second
            int gcd3 = Math.floorDiv(gcd2 * rofBonus, 1000);

            return Duration.ofMillis(10L * gcd3);
        }
    }

    public static final record Ability(Duration recast) implements CooldownStrategy {
        @Override
        public Duration getCooldown(ClientPlayerEntity player, MonkStats stats) {
            return recast;
        }
    }

}
