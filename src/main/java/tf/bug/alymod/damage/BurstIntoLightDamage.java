package tf.bug.alymod.damage;

import net.minecraft.entity.damage.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class BurstIntoLightDamage {

    public static Identifier ID =
            Identifier.of(Alymod.ID, "burst_into_light");

    public static RegistryKey<DamageType> KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, BurstIntoLightDamage.ID);

}
