package tf.bug.alymod.block;

import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.item.PrismaticFluidBucket;

public sealed abstract class PrismaticFluid extends FlowableFluid {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "prismatic_fluid");

    public static PrismaticFluid.Still STILL_FLUID =
            new PrismaticFluid.Still();

    public static final Identifier FLOWING_ID =
            Identifier.of(Alymod.ID, "flowing_prismatic_fluid");

    public static final PrismaticFluid.Flowing FLOWING_FLUID =
            new PrismaticFluid.Flowing();

    public static final TagKey<Fluid> TAG =
            TagKey.of(RegistryKeys.FLUID, PrismaticFluid.ID);

    public static final double SPEED =
            0.024d;


    @Override
    public Fluid getStill() {
        return PrismaticFluid.STILL_FLUID;
    }

    @Override
    public Fluid getFlowing() {
        return PrismaticFluid.FLOWING_FLUID;
    }

    @Override
    public Item getBucketItem() {
        return PrismaticFluidBucket.INSTANCE;
    }

    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getMaxFlowDistance(WorldView world) {
        return 1;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 8;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == PrismaticFluid.STILL_FLUID || fluid == PrismaticFluid.FLOWING_FLUID;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.isIn(PrismaticFluid.TAG);
    }

    @Override
    public int getTickRate(WorldView world) {
        return 16;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0f;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return PrismaticFluidBlock.INSTANCE.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    public final static class Still extends PrismaticFluid {

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

    }

    public final static class Flowing extends PrismaticFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(FlowableFluid.LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(FlowableFluid.LEVEL);
        }

    }

    public static void register() {
        Registry.register(Registries.FLUID, PrismaticFluid.ID, PrismaticFluid.STILL_FLUID);
        Registry.register(Registries.FLUID, PrismaticFluid.FLOWING_ID, PrismaticFluid.FLOWING_FLUID);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        PrismaticFluid.Renderer renderer = new PrismaticFluid.Renderer();
        FluidRenderHandlerRegistry.INSTANCE.register(PrismaticFluid.STILL_FLUID, PrismaticFluid.FLOWING_FLUID, renderer);
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), PrismaticFluid.STILL_FLUID, PrismaticFluid.FLOWING_FLUID);
        FluidVariantRendering.register(PrismaticFluid.STILL_FLUID, renderer);
    }

    @Environment(EnvType.CLIENT)
    public static final class Renderer extends SimpleFluidRenderHandler implements FluidVariantRenderHandler {
        public static final Identifier STILL =
                Identifier.of(Alymod.ID, "block/prismatic_fluid_still");

        public static final Identifier FLOWING =
                Identifier.of(Alymod.ID, "block/prismatic_fluid_flowing");

        public static final Identifier OVERLAY =
                Identifier.of(Alymod.ID, "block/prismatic_fluid_overlay");

        public Renderer() {
            super(PrismaticFluid.Renderer.STILL, PrismaticFluid.Renderer.FLOWING, PrismaticFluid.Renderer.OVERLAY);
        }

        private static double reverseLight(double l) {
            l = Math.sqrt(l * 0x0.ffp0d);
            final double shape = 1.52d, turning = 0.963d;
            final double d = turning - l;
            double r;
            if (d < 0)
                r = ((1.0 - turning) * (l - 1.0)) / (1.0 - (l + shape * d)) + 1.0;
            else
                r = (turning * l) / (1.0e-20d + (l + shape * d));
            return r;
        }

        private static double cube(double x) {
            return x * x * x;
        }

        private static double reverseGamma(double component) {
            return Math.sqrt(component);
        }

        @Override
        public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
            // Leave uncolored if we don't have a position
            if(pos == null) return -1;

            final int positionFactor = 8;
            final int positionMask = (1 << positionFactor) - 1;
            final double positionDivisor = (1 << positionFactor);

            int posAffect = (pos.getX() + pos.getY() + pos.getZ()) & positionMask;

            double h = (posAffect / positionDivisor) % 1;

            final double chroma = 0.11d;
            final double lightness = 70.0d / 100.0d;

            double a = chroma * Math.cos(2 * h * Math.PI);
            double b = chroma * Math.sin(2 * h * Math.PI);

            double rL = reverseLight(lightness);

            double l = cube(rL + 0.3963377774d * a + 0.2158037573d * b);
            double m = cube(rL - 0.1055613458d * a - 0.0638541728d * b);
            double s = cube(rL - 0.0894841775d * a - 1.2914855480d * b);

            int rr = (int) (reverseGamma(Math.min(Math.max(+4.0767245293d * l - 3.3072168827d * m + 0.2307590544d * s, 0d), 1d)) * (256d - Math.ulp(256d)));
            int gg = (int) (reverseGamma(Math.min(Math.max(-1.2681437731d * l + 2.6093323231d * m - 0.3411344290d * s, 0d), 1d)) * (256d - Math.ulp(256d)));
            int bb = (int) (reverseGamma(Math.min(Math.max(-0.0041119885d * l - 0.7034763098d * m + 1.7068625689d * s, 0d), 1d)) * (256d - Math.ulp(256d)));

            return (rr << 16) | (gg << 8) | (bb << 0);
        }

    }

}
