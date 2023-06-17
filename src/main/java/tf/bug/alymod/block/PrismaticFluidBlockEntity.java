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
import net.minecraft.client.render.block.FluidRenderer;
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

    boolean hasRendered;

    private PrismaticFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.hasRendered = false;
    }

    public PrismaticFluidBlockEntity(BlockPos pos, BlockState state) {
        this(PrismaticFluidBlockEntity.TYPE, pos, state);
    }

    public boolean isHasRendered() {
        return hasRendered;
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
                     return pfbe.isHasRendered();
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
            entity.hasRendered = true;

            BlockState blockStateSelf = entity.getCachedState();
            FluidState fluidStateSelf = blockStateSelf.getFluidState();

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

            boolean shouldRenderUp = !isOppositeSideRenderedPrismaticFluid(world, pos, Direction.UP, fluidStateUp);
            boolean shouldRenderDown = shouldRenderSide(world, pos, blockStateSelf, Direction.DOWN, fluidStateDown);
            boolean shouldRenderNorth = shouldRenderSide(world, pos, blockStateSelf, Direction.NORTH, fluidStateNorth);
            boolean shouldRenderSouth = shouldRenderSide(world, pos, blockStateSelf, Direction.SOUTH, fluidStateSouth);
            boolean shouldRenderWest = shouldRenderSide(world, pos, blockStateSelf, Direction.WEST, fluidStateWest);
            boolean shouldRenderEast = shouldRenderSide(world, pos, blockStateSelf, Direction.EAST, fluidStateEast);

            if(shouldRenderUp || shouldRenderDown || shouldRenderNorth || shouldRenderSouth || shouldRenderWest || shouldRenderEast) {
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

                float brightnessDown = world.getBrightness(Direction.DOWN, true);
                float brightnessUp = world.getBrightness(Direction.UP, true);
                float brightnessNorth = world.getBrightness(Direction.NORTH, true);
                float brightnessWest = world.getBrightness(Direction.WEST, true);
                Fluid fluidSelf = fluidStateSelf.getFluid();
                float heightSelf = getFluidHeight(world, fluidSelf, pos, blockStateSelf, fluidStateSelf);
                float heightNE;
                float heightNW;
                float heightSE;
                float heightSW;
                if (heightSelf >= 1.0F) {
                    heightNE = 1.0F;
                    heightNW = 1.0F;
                    heightSE = 1.0F;
                    heightSW = 1.0F;
                } else {
                    float heightN = getFluidHeight(world, fluidSelf, pos.north(), blockStateNorth, fluidStateNorth);
                    float heightS = getFluidHeight(world, fluidSelf, pos.south(), blockStateSouth, fluidStateSouth);
                    float heightE = getFluidHeight(world, fluidSelf, pos.east(), blockStateEast, fluidStateEast);
                    float heightV = getFluidHeight(world, fluidSelf, pos.west(), blockStateWest, fluidStateWest);
                    heightNE = calculateFluidHeight(world, fluidSelf, heightSelf, heightN, heightE, pos.offset(Direction.NORTH).offset(Direction.EAST));
                    heightNW = calculateFluidHeight(world, fluidSelf, heightSelf, heightN, heightV, pos.offset(Direction.NORTH).offset(Direction.WEST));
                    heightSE = calculateFluidHeight(world, fluidSelf, heightSelf, heightS, heightE, pos.offset(Direction.SOUTH).offset(Direction.EAST));
                    heightSW = calculateFluidHeight(world, fluidSelf, heightSelf, heightS, heightV, pos.offset(Direction.SOUTH).offset(Direction.WEST));
                }

                float x = 0.001F;
                float y = shouldRenderDown ? 0.001F : 0.0F;

                float whiteULeft = background.getFrameU(0.0);
                float whiteURight = background.getFrameU(16.0);
                float whiteVTop = background.getFrameV(0.0);
                float whiteVBot = background.getFrameV(16.0);

                if (shouldRenderUp && !isSideCovered(world, pos, Direction.UP, Math.min(Math.min(heightNW, heightSW), Math.min(heightSE, heightNE)), blockStateUp)) {
                    heightNW -= 0.001F;
                    heightSW -= 0.001F;
                    heightSE -= 0.001F;
                    heightNE -= 0.001F;

                    int worldLight = getLight(world, pos);
                    float r = baseR * brightnessUp;
                    float g = baseG * brightnessUp;
                    float b = baseB * brightnessUp;
                    vertex(model, vertexConsumer, 0.0f, heightNW, 0.0f, r, g, b, baseA, whiteULeft, whiteVTop, worldLight);
                    vertex(model, vertexConsumer, 0.0f, heightSW, 1.0f, r, g, b, baseA, whiteULeft, whiteVBot, worldLight);
                    vertex(model, vertexConsumer, 1.0f, heightSE, 1.0f, r, g, b, baseA, whiteURight, whiteVBot, worldLight);
                    vertex(model, vertexConsumer, 1.0f, heightNE, 0.0f, r, g, b, baseA, whiteURight, whiteVTop, worldLight);
                    if (fluidStateSelf.canFlowTo(world, pos.up())) {
                        vertex(model, vertexConsumer, 0.0f, heightNW, 0.0f, r, g, b, baseA, whiteULeft, whiteVTop, worldLight);
                        vertex(model, vertexConsumer, 1.0f, heightNE, 0.0f, r, g, b, baseA, whiteULeft, whiteVBot, worldLight);
                        vertex(model, vertexConsumer, 1.0f, heightSE, 1.0f, r, g, b, baseA, whiteURight, whiteVBot, worldLight);
                        vertex(model, vertexConsumer, 0.0f, heightSW, 1.0f, r, g, b, baseA, whiteURight, whiteVTop, worldLight);
                    }
                }

                if (shouldRenderDown) {
                    int worldLight = getLight(world, pos.down());
                    float r = baseR * brightnessDown;
                    float g = baseG * brightnessDown;
                    float b = baseB * brightnessDown;
                    vertex(model, vertexConsumer, 0.0f, y, 1.0f, r, g, b, baseA, whiteULeft, whiteVTop, worldLight);
                    vertex(model, vertexConsumer, 0.0f, y, 0.0f, r, g, b, baseA, whiteULeft, whiteVBot, worldLight);
                    vertex(model, vertexConsumer, 1.0f, y, 0.0f, r, g, b, baseA, whiteURight, whiteVBot, worldLight);
                    vertex(model, vertexConsumer, 1.0f, y, 1.0f, r, g, b, baseA, whiteURight, whiteVTop, worldLight);
                }

                int aq = getLight(world, pos);
                Iterator<Direction> sides = Direction.Type.HORIZONTAL.iterator();

                drawSides: while(true) {
                    Direction direction;
                    float x1;
                    float x2;
                    float y1;
                    float y2;
                    float height1;
                    float height2;
                    boolean shouldRender;
                    do {
                        do {
                            if (!sides.hasNext()) {
                                break drawSides;
                            }

                            direction = sides.next();
                            switch (direction) {
                                case NORTH -> {
                                    height1 = heightNW;
                                    height2 = heightNE;
                                    x1 = 0.0f;
                                    x2 = 1.0f;
                                    y1 = 0.001f;
                                    y2 = 0.001f;
                                    shouldRender = shouldRenderNorth;
                                }
                                case SOUTH -> {
                                    height1 = heightSE;
                                    height2 = heightSW;
                                    x1 = 1.0f;
                                    x2 = 0.0f;
                                    y1 = 1.0f - 0.001f;
                                    y2 = 1.0f - 0.001f;
                                    shouldRender = shouldRenderSouth;
                                }
                                case WEST -> {
                                    height1 = heightSW;
                                    height2 = heightNW;
                                    x1 = 0.001f;
                                    x2 = 0.001f;
                                    y1 = 1.0f;
                                    y2 = 0.0f;
                                    shouldRender = shouldRenderWest;
                                }
                                default -> {
                                    height1 = heightNE;
                                    height2 = heightSE;
                                    x1 = 1.0f - 0.001f;
                                    x2 = 1.0f - 0.001f;
                                    y1 = 0.0f;
                                    y2 = 1.0f;
                                    shouldRender = shouldRenderEast;
                                }
                            }
                        } while(!shouldRender);
                    } while(isSideCovered(world, pos, direction, Math.max(height1, height2), world.getBlockState(pos.offset(direction))));

                    BlockPos blockPos = pos.offset(direction);
                    Block block = world.getBlockState(blockPos).getBlock();
                    boolean drawOverlay = block instanceof TransparentBlock || block instanceof LeavesBlock;

                    float brightness = direction.getAxis() == Direction.Axis.Z ? brightnessNorth : brightnessWest;
                    float r = baseR * brightnessUp * brightness;
                    float g = baseG * brightnessUp * brightness;
                    float b = baseB * brightnessUp * brightness;
                    // TODO what is this?
                    vertex(model, vertexConsumer, x1, height1, y1, r, g, b, baseA, whiteULeft, whiteVTop, aq);
                    vertex(model, vertexConsumer, x2, height2, y2, r, g, b, baseA, whiteURight, whiteVTop, aq);
                    vertex(model, vertexConsumer, x2, y, y2, r, g, b, baseA, whiteURight, whiteVBot, aq);
                    vertex(model, vertexConsumer, x1, y, y1, r, g, b, baseA, whiteULeft, whiteVBot, aq);
                    if (!drawOverlay) {
                        vertex(model, vertexConsumer, x1, y, y1, r, g, b, baseA, whiteULeft, whiteVTop, aq);
                        vertex(model, vertexConsumer, x2, y, y2, r, g, b, baseA, whiteURight, whiteVTop, aq);
                        vertex(model, vertexConsumer, x2, height2, y2, r, g, b, baseA, whiteURight, whiteVBot, aq);
                        vertex(model, vertexConsumer, x1, height1, y1, r, g, b, baseA, whiteULeft, whiteVBot, aq);
                    }
                }

                matrices.pop();
            }
        }

    }

}
