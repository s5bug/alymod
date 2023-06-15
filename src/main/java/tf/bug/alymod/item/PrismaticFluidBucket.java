package tf.bug.alymod.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.block.PrismaticFluid;

public class PrismaticFluidBucket extends BucketItem {

    private PrismaticFluidBucket(Fluid fluid, Settings settings) {
        super(fluid, settings);
    }

    public static Item.Settings SETTINGS =
            new FabricItemSettings()
                    .recipeRemainder(Items.BUCKET)
                    .maxCount(1);

    public static Identifier ID =
            Identifier.of(Alymod.ID, "prismatic_fluid_bucket");

    public static PrismaticFluidBucket INSTANCE =
            new PrismaticFluidBucket(PrismaticFluid.STILL_FLUID, SETTINGS);

    public static void register() {
        Registry.register(Registries.ITEM, PrismaticFluidBucket.ID, PrismaticFluidBucket.INSTANCE);
    }

}
