package tf.bug.alymod.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import tf.bug.alymod.Alymod;

public class PrismaticShard extends Item {
    private PrismaticShard(Settings settings) {
        super(settings);
    }

    public static Item.Settings SETTINGS =
            new FabricItemSettings()
                    .food(new FoodComponent.Builder()
                            .snack()
                            .alwaysEdible()
                            .hunger(1)
                            .saturationModifier(0.1F)
                            .build());

    public static Identifier ID =
            Identifier.of(Alymod.ID, "prismatic_shard");

    public static PrismaticShard INSTANCE =
            new PrismaticShard(SETTINGS);

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        world.createExplosion(null, null, null, user.getPos(), 12.0F, false, World.ExplosionSourceType.NONE);
        return result;
    }

    public static void register() {
        Registry.register(Registries.ITEM, PrismaticShard.ID, PrismaticShard.INSTANCE);
    }

}
