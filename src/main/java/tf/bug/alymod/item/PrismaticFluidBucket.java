package tf.bug.alymod.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.block.PrismaticFluid;

public class PrismaticFluidBucket extends BucketItem {

    private Fluid fluid;

    private PrismaticFluidBucket(Fluid fluid, Settings settings) {
        super(fluid, settings);
        this.fluid = fluid;
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
