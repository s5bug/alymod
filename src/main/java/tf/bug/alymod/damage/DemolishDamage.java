package tf.bug.alymod.damage;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class DemolishDamage {

    public static Identifier ID =
            Identifier.of(Alymod.ID, "demolish");

    public static RegistryKey<DamageType> KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, DemolishDamage.ID);

}
