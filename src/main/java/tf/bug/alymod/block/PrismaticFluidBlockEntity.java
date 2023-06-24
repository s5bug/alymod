package tf.bug.alymod.block;

import java.util.Iterator;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import tf.bug.alymod.Alymod;

public class PrismaticFluidBlockEntity extends BlockEntity {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "prismatic_fluid");

    public static BlockEntityType<PrismaticFluidBlockEntity> TYPE =
            FabricBlockEntityTypeBuilder.create(PrismaticFluidBlockEntity::new, PrismaticFluidBlock.INSTANCE)
                    .build();

    private boolean isRenderDirty;
    private float cachedHeightNE;
    private float cachedHeightNW;
    private float cachedHeightSE;
    private float cachedHeightSW;
    private float cachedBrightnessUp;
    private float cachedBrightnessDown;
    private float cachedBrightnessNorth;
    private float cachedBrightnessWest;
    private boolean cachedRenderUp;
    private boolean cachedCoveredUp;
    private boolean cachedRenderDown;
    private boolean cachedRenderNorth;
    private boolean cachedCoveredNorth;
    private boolean cachedRenderSouth;
    private boolean cachedCoveredSouth;
    private boolean cachedRenderWest;
    private boolean cachedCoveredWest;
    private boolean cachedRenderEast;
    private boolean cachedCoveredEast;
    private boolean cachedRender;
    private int cachedWorldLightUp;
    private int cachedWorldLightDown;
    private boolean isRendered;

    private PrismaticFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.isRenderDirty = true;
        this.isRendered = false;
    }

    public PrismaticFluidBlockEntity(BlockPos pos, BlockState state) {
        this(PrismaticFluidBlockEntity.TYPE, pos, state);
    }

    public void markRenderDirty() {
        this.isRenderDirty = true;
    }

    public boolean isRendered() {
        return this.isRendered;
    }

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, PrismaticFluidBlockEntity.ID, PrismaticFluidBlockEntity.TYPE);
    }

    public static void registerClient() {
        BlockEntityRendererFactories.register(PrismaticFluidBlockEntity.TYPE, PrismaticFluidBlockEntity.Renderer::new);
    }

    public static class Renderer implements BlockEntityRenderer<PrismaticFluidBlockEntity> {

        public Renderer(BlockEntityRendererFactory.Context ctx) {}

        private static float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos) {
            if (!(eastWestHeight >= 1.0F) && !(northSouthHeight >= 1.0F)) {
                float[] fs = new float[2];
                if (eastWestHeight > 0.0F || northSouthHeight > 0.0F) {
                    float f = getFluidHeight(world, fluid, pos);
                    if (f >= 1.0F) {
                        return 1.0F;
                    }

                    addHeight(fs, f);
                }

                addHeight(fs, originHeight);
                addHeight(fs, eastWestHeight);
                addHeight(fs, northSouthHeight);
                return fs[0] / fs[1];
            } else {
                return 1.0F;
            }
        }

        private static void addHeight(float[] weightedAverageHeight, float height) {
            if (height >= 0.8F) {
                weightedAverageHeight[0] += height * 10.0F;
                weightedAverageHeight[1] += 10.0F;
            } else if (height >= 0.0F) {
                weightedAverageHeight[0] += height;
                weightedAverageHeight[1] += 1.0F;
            }
        }

        private static float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            return getFluidHeight(world, fluid, pos, blockState, blockState.getFluidState());
        }

        private static float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
            if (fluid.matchesType(fluidState.getFluid())) {
                BlockState blockState2 = world.getBlockState(pos.up());
                return fluid.matchesType(blockState2.getFluidState().getFluid()) ? 1.0F : fluidState.getHeight();
            } else {
                return !blockState.isSolid() ? 0.0F : -1.0F;
            }
        }

        private static boolean isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state) {
            if (state.isOpaque()) {
                VoxelShape voxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, height, 1.0);
                VoxelShape voxelShape2 = state.getCullingShape(world, pos);
                return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
            } else {
                return false;
            }
        }

        private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state) {
            return isSideCovered(world, direction, maxDeviation, pos.offset(direction), state);
        }

        private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
            return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
        }

        private static boolean isOppositeSideRenderedPrismaticFluid(BlockRenderView world, BlockPos pos, Direction direction, FluidState neighborFluidState) {
            if(neighborFluidState.isIn(PrismaticFluid.TAG)) {
                 if(world.getBlockEntity(pos.offset(direction)) instanceof PrismaticFluidBlockEntity pfbe) {
                     return pfbe.isRendered();
                 } else {
                     return false;
                 }
            } else {
                return false;
            }
        }

        private static boolean shouldRenderSide(BlockRenderView world, BlockPos pos, BlockState blockState, Direction direction, FluidState neighborFluidState) {
            return !(isOppositeSideCovered(world, pos, blockState, direction) || isOppositeSideRenderedPrismaticFluid(world, pos, direction, neighborFluidState));
        }

        private static int getLight(BlockRenderView world, BlockPos pos) {
            int i = WorldRenderer.getLightmapCoordinates(world, pos);
            int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
            int k = i & 255;
            int l = j & 255;
            int m = i >> 16 & 255;
            int n = j >> 16 & 255;
            return ((Math.max(m, n)) << 16) | (Math.max(k, l));
        }

        private static void vertex(Matrix4f model, VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int light) {
            vertexConsumer.vertex(model, x, y, z)
                    .color(red, green, blue, alpha)
                    .texture(u, v)
                    .light(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .next();
        }

        @Override
        public int getRenderDistance() {
            // Assume 32 (+ 1) chunk radius, at 16 blocks per chunk
            return Math.max(BlockEntityRenderer.super.getRenderDistance(), 528);
        }

        @Override
        public void render(PrismaticFluidBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
            World world = entity.world;
            BlockPos pos = entity.pos;
            BlockState blockStateSelf = entity.getCachedState();
            FluidState fluidStateSelf = blockStateSelf.getFluidState();

            if(entity.isRenderDirty) {
                entity.isRenderDirty = false;

                BlockState blockStateDown = world.getBlockState(pos.offset(Direction.DOWN));
                FluidState fluidStateDown = blockStateDown.getFluidState();
                BlockState blockStateUp = world.getBlockState(pos.offset(Direction.UP));
                FluidState fluidStateUp = blockStateUp.getFluidState();
                BlockState blockStateNorth = world.getBlockState(pos.offset(Direction.NORTH));
                FluidState fluidStateNorth = blockStateNorth.getFluidState();
                BlockState blockStateSouth = world.getBlockState(pos.offset(Direction.SOUTH));
                FluidState fluidStateSouth = blockStateSouth.getFluidState();
                BlockState blockStateWest = world.getBlockState(pos.offset(Direction.WEST));
                FluidState fluidStateWest = blockStateWest.getFluidState();
                BlockState blockStateEast = world.getBlockState(pos.offset(Direction.EAST));
                FluidState fluidStateEast = blockStateEast.getFluidState();

                if(!entity.isRendered) {
                    entity.isRendered = true;
                    if(world.getBlockEntity(pos.offset(Direction.DOWN)) instanceof PrismaticFluidBlockEntity pfbeDown) {
                        pfbeDown.markRenderDirty();
                    }
                    if(world.getBlockEntity(pos.offset(Direction.UP)) instanceof PrismaticFluidBlockEntity pfbeUp) {
                        pfbeUp.markRenderDirty();
                    }
                    if(world.getBlockEntity(pos.offset(Direction.NORTH)) instanceof PrismaticFluidBlockEntity pfbeNorth) {
                        pfbeNorth.markRenderDirty();
                    }
                    if(world.getBlockEntity(pos.offset(Direction.SOUTH)) instanceof PrismaticFluidBlockEntity pfbeSouth) {
                        pfbeSouth.markRenderDirty();
                    }
                    if(world.getBlockEntity(pos.offset(Direction.WEST)) instanceof PrismaticFluidBlockEntity pfbeWest) {
                        pfbeWest.markRenderDirty();
                    }
                    if(world.getBlockEntity(pos.offset(Direction.EAST)) instanceof PrismaticFluidBlockEntity pfbeEast) {
                        pfbeEast.markRenderDirty();
                    }
                }

                entity.cachedRenderUp = !isOppositeSideRenderedPrismaticFluid(world, pos, Direction.UP, fluidStateUp);
                entity.cachedRenderDown = shouldRenderSide(world, pos, blockStateSelf, Direction.DOWN, fluidStateDown);
                entity.cachedRenderNorth = shouldRenderSide(world, pos, blockStateSelf, Direction.NORTH, fluidStateNorth);
                entity.cachedRenderSouth = shouldRenderSide(world, pos, blockStateSelf, Direction.SOUTH, fluidStateSouth);
                entity.cachedRenderWest = shouldRenderSide(world, pos, blockStateSelf, Direction.WEST, fluidStateWest);
                entity.cachedRenderEast = shouldRenderSide(world, pos, blockStateSelf, Direction.EAST, fluidStateEast);

                entity.cachedRender = entity.cachedRenderUp ||
                        entity.cachedRenderDown ||
                        entity.cachedRenderNorth ||
                        entity.cachedRenderSouth ||
                        entity.cachedRenderWest ||
                        entity.cachedRenderEast;

                if(entity.cachedRender) {
                    entity.cachedBrightnessDown = world.getBrightness(Direction.DOWN, true);
                    entity.cachedBrightnessUp = world.getBrightness(Direction.UP, true);
                    entity.cachedBrightnessNorth = world.getBrightness(Direction.NORTH, true);
                    entity.cachedBrightnessWest = world.getBrightness(Direction.WEST, true);

                    Fluid fluidSelf = fluidStateSelf.getFluid();
                    float heightSelf = getFluidHeight(world, fluidSelf, pos, blockStateSelf, fluidStateSelf);

                    if (heightSelf >= 1.0F) {
                        entity.cachedHeightNE = 1.0F;
                        entity.cachedHeightNW = 1.0F;
                        entity.cachedHeightSE = 1.0F;
                        entity.cachedHeightSW = 1.0F;
                    } else {
                        float heightN = getFluidHeight(world, fluidSelf, pos.north(), blockStateNorth, fluidStateNorth);
                        float heightS = getFluidHeight(world, fluidSelf, pos.south(), blockStateSouth, fluidStateSouth);
                        float heightE = getFluidHeight(world, fluidSelf, pos.east(), blockStateEast, fluidStateEast);
                        float heightV = getFluidHeight(world, fluidSelf, pos.west(), blockStateWest, fluidStateWest);
                        entity.cachedHeightNE = calculateFluidHeight(world, fluidSelf, heightSelf, heightN, heightE, pos.offset(Direction.NORTH).offset(Direction.EAST));
                        entity.cachedHeightNW = calculateFluidHeight(world, fluidSelf, heightSelf, heightN, heightV, pos.offset(Direction.NORTH).offset(Direction.WEST));
                        entity.cachedHeightSE = calculateFluidHeight(world, fluidSelf, heightSelf, heightS, heightE, pos.offset(Direction.SOUTH).offset(Direction.EAST));
                        entity.cachedHeightSW = calculateFluidHeight(world, fluidSelf, heightSelf, heightS, heightV, pos.offset(Direction.SOUTH).offset(Direction.WEST));
                    }

                    if(entity.cachedRenderUp) { // FIXME
                        entity.cachedWorldLightUp = getLight(world, pos);
                        entity.cachedCoveredUp = isSideCovered(world, pos, Direction.UP, Math.min(Math.min(entity.cachedHeightNW, entity.cachedHeightSW), Math.min(entity.cachedHeightSE, entity.cachedHeightNE)), blockStateUp);
                    }

                    if(entity.cachedRenderDown) {
                        entity.cachedWorldLightDown = getLight(world, pos.down());
                    }

                    if(entity.cachedRenderNorth) {
                        entity.cachedCoveredNorth = isSideCovered(world, pos, Direction.NORTH, Math.max(entity.cachedHeightNW, entity.cachedHeightNE), blockStateNorth);
                    }
                    if(entity.cachedRenderSouth) {
                        entity.cachedCoveredSouth = isSideCovered(world, pos, Direction.SOUTH, Math.max(entity.cachedHeightSW, entity.cachedHeightSE), blockStateSouth);
                    }
                    if(entity.cachedRenderWest) {
                        entity.cachedCoveredWest = isSideCovered(world, pos, Direction.WEST, Math.max(entity.cachedHeightNW, entity.cachedHeightSW), blockStateWest);
                    }
                    if(entity.cachedRenderEast) {
                        entity.cachedCoveredEast = isSideCovered(world, pos, Direction.EAST, Math.max(entity.cachedHeightNE, entity.cachedHeightSE), blockStateEast);
                    }
                }
            }

            if(entity.cachedRender) {
                matrices.push();

                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

                PrismaticFluid.Renderer original =
                        (PrismaticFluid.Renderer) FluidRenderHandlerRegistry.INSTANCE.get(PrismaticFluid.STILL_FLUID);
                Sprite background = original.getBackground();
                Sprite star = original.getStar();

                int fluidBaseColor = original.getFluidColor(world, pos, fluidStateSelf);
                // Interestingly, the renderer doesn't like the alpha set in the Sprite
                // TODO figure out how to pull the alpha from the texture
                float baseA = ((fluidBaseColor >>> 24) & 0xFF) / 255.0F;
                float baseR = ((fluidBaseColor >>> 16) & 0xFF) / 255.0F;
                float baseG = ((fluidBaseColor >>> 8) & 0xFF) / 255.0F;
                float baseB = (fluidBaseColor & 0xFF) / 255.0F;

                Matrix4f model = matrices.peek().getPositionMatrix();

                float x = 0.001F;
                float y = entity.cachedRenderDown ? 0.001F : 0.0F;

                float whiteULeft = background.getFrameU(0.0);
                float whiteURight = background.getFrameU(16.0);
                float whiteVTop = background.getFrameV(0.0);
                float whiteVBot = background.getFrameV(16.0);

                if (entity.cachedRenderUp && !entity.cachedCoveredUp) {
                    float heightNW = entity.cachedHeightNW - 0.001F;
                    float heightSW = entity.cachedHeightSW - 0.001F;
                    float heightSE = entity.cachedHeightSE - 0.001F;
                    float heightNE = entity.cachedHeightNE - 0.001F;

                    float r = baseR * entity.cachedBrightnessUp;
                    float g = baseG * entity.cachedBrightnessUp;
                    float b = baseB * entity.cachedBrightnessUp;

                    vertex(model, vertexConsumer, 0.0f, heightNW, 0.0f, r, g, b, baseA, whiteULeft, whiteVTop, entity.cachedWorldLightUp);
                    vertex(model, vertexConsumer, 0.0f, heightSW, 1.0f, r, g, b, baseA, whiteULeft, whiteVBot, entity.cachedWorldLightUp);
                    vertex(model, vertexConsumer, 1.0f, heightSE, 1.0f, r, g, b, baseA, whiteURight, whiteVBot, entity.cachedWorldLightUp);
                    vertex(model, vertexConsumer, 1.0f, heightNE, 0.0f, r, g, b, baseA, whiteURight, whiteVTop, entity.cachedWorldLightUp);
                    if (fluidStateSelf.canFlowTo(world, pos.up())) {
                        // FIXME why does it draw twice
                        vertex(model, vertexConsumer, 0.0f, heightNW, 0.0f, r, g, b, baseA, whiteULeft, whiteVTop, entity.cachedWorldLightUp);
                        vertex(model, vertexConsumer, 1.0f, heightNE, 0.0f, r, g, b, baseA, whiteULeft, whiteVBot, entity.cachedWorldLightUp);
                        vertex(model, vertexConsumer, 1.0f, heightSE, 1.0f, r, g, b, baseA, whiteURight, whiteVBot, entity.cachedWorldLightUp);
                        vertex(model, vertexConsumer, 0.0f, heightSW, 1.0f, r, g, b, baseA, whiteURight, whiteVTop, entity.cachedWorldLightUp);
                    }
                }

                if (entity.cachedRenderDown) {
                    float r = baseR * entity.cachedBrightnessDown;
                    float g = baseG * entity.cachedBrightnessDown;
                    float b = baseB * entity.cachedBrightnessDown;
                    vertex(model, vertexConsumer, 0.0f, y, 1.0f, r, g, b, baseA, whiteULeft, whiteVTop, entity.cachedWorldLightDown);
                    vertex(model, vertexConsumer, 0.0f, y, 0.0f, r, g, b, baseA, whiteULeft, whiteVBot, entity.cachedWorldLightDown);
                    vertex(model, vertexConsumer, 1.0f, y, 0.0f, r, g, b, baseA, whiteURight, whiteVBot, entity.cachedWorldLightDown);
                    vertex(model, vertexConsumer, 1.0f, y, 1.0f, r, g, b, baseA, whiteURight, whiteVTop, entity.cachedWorldLightDown);
                }

                for (Direction direction : Direction.Type.HORIZONTAL) {
                    float x1;
                    float x2;
                    float z1;
                    float z2;
                    float y1;
                    float y2;

                    boolean shouldRender;
                    boolean isCovered;

                    switch (direction) {
                        case NORTH -> {
                            y1 = entity.cachedHeightNW;
                            y2 = entity.cachedHeightNE;
                            x1 = 0.0f;
                            x2 = 1.0f;
                            z1 = 0.001f;
                            z2 = 0.001f;
                            shouldRender = entity.cachedRenderNorth;
                            isCovered = entity.cachedCoveredNorth;
                        }
                        case SOUTH -> {
                            y1 = entity.cachedHeightSE;
                            y2 = entity.cachedHeightSW;
                            x1 = 1.0f;
                            x2 = 0.0f;
                            z1 = 1.0f - 0.001f;
                            z2 = 1.0f - 0.001f;
                            shouldRender = entity.cachedRenderSouth;
                            isCovered = entity.cachedCoveredSouth;
                        }
                        case WEST -> {
                            y1 = entity.cachedHeightSW;
                            y2 = entity.cachedHeightNW;
                            x1 = 0.001f;
                            x2 = 0.001f;
                            z1 = 1.0f;
                            z2 = 0.0f;
                            shouldRender = entity.cachedRenderWest;
                            isCovered = entity.cachedCoveredWest;
                        }
                        default -> {
                            y1 = entity.cachedHeightNE;
                            y2 = entity.cachedHeightSE;
                            x1 = 1.0f - 0.001f;
                            x2 = 1.0f - 0.001f;
                            z1 = 0.0f;
                            z2 = 1.0f;
                            shouldRender = entity.cachedRenderEast;
                            isCovered = entity.cachedCoveredEast;
                        }
                    }

                    if (!shouldRender || isCovered) continue;

                    float brightness = direction.getAxis() == Direction.Axis.Z ? entity.cachedBrightnessNorth : entity.cachedBrightnessWest;
                    float r = baseR * entity.cachedBrightnessUp * brightness;
                    float g = baseG * entity.cachedBrightnessUp * brightness;
                    float b = baseB * entity.cachedBrightnessUp * brightness;
                    // TODO what is this?
                    vertex(model, vertexConsumer, x1, y1, z1, r, g, b, baseA, whiteULeft, whiteVTop, entity.cachedWorldLightUp);
                    vertex(model, vertexConsumer, x2, y2, z2, r, g, b, baseA, whiteURight, whiteVTop, entity.cachedWorldLightUp);
                    vertex(model, vertexConsumer, x2, y, z2, r, g, b, baseA, whiteURight, whiteVBot, entity.cachedWorldLightUp);
                    vertex(model, vertexConsumer, x1, y, z1, r, g, b, baseA, whiteULeft, whiteVBot, entity.cachedWorldLightUp);
                    if (true) { // if(!drawOverlay)
                        vertex(model, vertexConsumer, x1, y, z1, r, g, b, baseA, whiteULeft, whiteVTop, entity.cachedWorldLightUp);
                        vertex(model, vertexConsumer, x2, y, z2, r, g, b, baseA, whiteURight, whiteVTop, entity.cachedWorldLightUp);
                        vertex(model, vertexConsumer, x2, y2, z2, r, g, b, baseA, whiteURight, whiteVBot, entity.cachedWorldLightUp);
                        vertex(model, vertexConsumer, x1, y1, z1, r, g, b, baseA, whiteULeft, whiteVBot, entity.cachedWorldLightUp);
                    }
                }

                matrices.pop();
            }
        }

    }

}
