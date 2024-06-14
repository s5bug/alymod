package tf.bug.alymod.block;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.Alymod;

public class PrismaticFluidBlock extends FluidBlock {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "prismatic_fluid");

    public static final AbstractBlock.Settings SETTINGS =
            FabricBlockSettings.create()
                    .replaceable()
                    .noCollision()
                    .strength(100.0F)
                    .dropsNothing()
                    .liquid();

    public static final Block INSTANCE =
            new PrismaticFluidBlock(PrismaticFluid.STILL_FLUID, SETTINGS);

    public PrismaticFluidBlock(PrismaticFluid prismaticFluid, AbstractBlock.Settings settings) {
        super(prismaticFluid, settings);
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return false; // TODO why?
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return true;
    }

    public static void register() {
        Registry.register(Registries.BLOCK, PrismaticFluidBlock.ID, PrismaticFluidBlock.INSTANCE);
    }

}
