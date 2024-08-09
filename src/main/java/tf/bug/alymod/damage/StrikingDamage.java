package tf.bug.alymod.damage;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class StrikingDamage {

    public static Identifier ID =
            Identifier.of(Alymod.ID, "striking");

    public static RegistryKey<DamageType> KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, StrikingDamage.ID);

}
