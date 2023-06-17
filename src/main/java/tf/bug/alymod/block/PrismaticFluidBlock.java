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

public class PrismaticFluidBlock extends BlockWithEntity implements FluidDrainable {

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

    private final PrismaticFluid prismaticFluid;
    private final List<FluidState> statesByLevel;

    public PrismaticFluidBlock(PrismaticFluid prismaticFluid, AbstractBlock.Settings settings) {
        super(settings);
        this.prismaticFluid = prismaticFluid;

        this.statesByLevel = new ArrayList<>(8);
        this.statesByLevel.add(this.prismaticFluid.getStill(false));
        for(int i = 1; i < 8; ++i) {
            this.statesByLevel.add(this.prismaticFluid.getFlowing(8 - i, false));
        }
        this.statesByLevel.add(this.prismaticFluid.getFlowing(8, true));

        this.setDefaultState((this.stateManager.getDefaultState()).with(FluidBlock.LEVEL, 0));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FluidBlock.LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PrismaticFluidBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return context.isAbove(FluidBlock.COLLISION_SHAPE, pos, true) && state.get(FluidBlock.LEVEL) == 0 && context.canWalkOnFluid(world.getFluidState(pos.up()), state.getFluidState()) ? FluidBlock.COLLISION_SHAPE : VoxelShapes.empty();
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.getFluidState().hasRandomTicks();
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        state.getFluidState().onRandomTick(world, pos, random);
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return false; // TODO why?
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return true;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        int i = state.get(FluidBlock.LEVEL);
        return this.statesByLevel.get(Math.min(i, 8));
    }

    @Override
    public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
        if (state.get(FluidBlock.LEVEL) == 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            return new ItemStack(this.prismaticFluid.getBucketItem());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.getFluidState().getFluid().matchesType(this.prismaticFluid);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return Collections.emptyList();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return this.prismaticFluid.getBucketFillSound();
    }

    public static void register() {
        Registry.register(Registries.BLOCK, PrismaticFluidBlock.ID, PrismaticFluidBlock.INSTANCE);
    }

}
