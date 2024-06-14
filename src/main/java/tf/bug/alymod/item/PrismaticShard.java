package tf.bug.alymod.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.damage.BurstIntoLightDamage;
import tf.bug.alymod.effect.ChromaticAberrationStatusEffect;

public class PrismaticShard extends Item {
    private PrismaticShard(Settings settings) {
        super(settings);
    }

    public static Item.Settings SETTINGS =
            new Item.Settings()
                    .food(new FoodComponent.Builder()
                            .snack()
                            .alwaysEdible()
                            .nutrition(1)
                            .saturationModifier(0.1F)
                            .build());

    public static Identifier ID =
            Identifier.of(Alymod.ID, "prismatic_shard");

    public static PrismaticShard INSTANCE =
            new PrismaticShard(SETTINGS);

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);

        int amp = 0;
        if(user.hasStatusEffect(ChromaticAberrationStatusEffect.INSTANCE.reference())) {
            amp = 1 + user.getStatusEffect(ChromaticAberrationStatusEffect.INSTANCE.reference()).getAmplifier();
        }

        user.addStatusEffect(new StatusEffectInstance(
                ChromaticAberrationStatusEffect.INSTANCE.reference(),
                320,
                amp
        ));

        user.damage(user.getWorld().getDamageSources().create(BurstIntoLightDamage.KEY), 2.0f);

        return result;
    }

    public static void register() {
        Registry.register(Registries.ITEM, PrismaticShard.ID, PrismaticShard.INSTANCE);
    }

}
