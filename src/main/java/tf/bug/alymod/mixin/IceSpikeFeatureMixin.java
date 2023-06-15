package tf.bug.alymod.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IceSpikeFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.bug.alymod.block.PrismaticIce;

@Mixin(IceSpikeFeature.class)
public abstract class IceSpikeFeatureMixin extends Feature<DefaultFeatureConfig> {

    private IceSpikeFeatureMixin(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    /**
     * @author Aly
     * @reason need to inject custom blocks in the very center of an ice spike, depending on a variable defined at the
     *         start of the ice spike
     */
    @Overwrite
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();

        StructureWorldAccess structureWorldAccess = context.getWorld();
        while(structureWorldAccess.isAir(blockPos) && blockPos.getY() > structureWorldAccess.getBottomY() + 2) {
            blockPos = blockPos.down();
        }

        if (!structureWorldAccess.getBlockState(blockPos).isOf(Blocks.SNOW_BLOCK)) {
            return false;
        } else {
            boolean hasPrismaticIce = random.nextInt(80) == 0;

            blockPos = blockPos.up(random.nextInt(4));
            int heightFactor = random.nextInt(4) + 7;
            int baseFactor = heightFactor / 4 + random.nextInt(2);
            if (baseFactor > 1 && random.nextInt(60) == 0) {
                hasPrismaticIce = true;
                blockPos = blockPos.up(10 + random.nextInt(30));
            }

            for(int y = 0; y < heightFactor; ++y) {
                float radiusf = (1.0F - (float)y / (float)heightFactor) * (float)baseFactor;
                int radius = MathHelper.ceil(radiusf);

                for(int x = -radius; x <= radius; ++x) {
                    float xf = (float)MathHelper.abs(x) - 0.25F;

                    for(int z = -radius; z <= radius; ++z) {
                        float zf = (float)MathHelper.abs(z) - 0.25F;
                        if ((x == 0 && z == 0 || !(xf * xf + zf * zf > radiusf * radiusf)) && (x != -radius && x != radius && z != -radius && z != radius || random.nextFloat() <= 0.75F)) {
                            BlockState blockState = structureWorldAccess.getBlockState(blockPos.add(x, y, z));
                            if (blockState.isAir() || Feature.isSoil(blockState) || blockState.isOf(Blocks.SNOW_BLOCK) || blockState.isOf(Blocks.ICE)) {
                                this.setBlockState(structureWorldAccess, blockPos.add(x, y, z), Blocks.PACKED_ICE.getDefaultState());
                            }

                            if (y != 0 && radius > 1) {
                                blockState = structureWorldAccess.getBlockState(blockPos.add(x, -y, z));
                                if (blockState.isAir() || isSoil(blockState) || blockState.isOf(Blocks.SNOW_BLOCK) || blockState.isOf(Blocks.ICE)) {
                                    this.setBlockState(structureWorldAccess, blockPos.add(x, -y, z), Blocks.PACKED_ICE.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }

            int ringFactor = baseFactor - 1;
            if (ringFactor < 0) {
                ringFactor = 0;
            } else if (ringFactor > 1) {
                ringFactor = 1;
            }

            for(int x = -ringFactor; x <= ringFactor; ++x) {
                for(int z = -ringFactor; z <= ringFactor; ++z) {
                    BlockPos blockPos2 = blockPos.add(x, -1, z);
                    int p = 50;
                    if (Math.abs(x) == 1 && Math.abs(z) == 1) {
                        p = random.nextInt(5);
                    }

                    while(blockPos2.getY() > 50) {
                        BlockState blockState2 = structureWorldAccess.getBlockState(blockPos2);
                        if (!blockState2.isAir() && !isSoil(blockState2) && !blockState2.isOf(Blocks.SNOW_BLOCK) && !blockState2.isOf(Blocks.ICE) && !blockState2.isOf(Blocks.PACKED_ICE)) {
                            break;
                        }

                        if(x == 0 && z == 0 && hasPrismaticIce && random.nextInt(5) == 0) {
                            this.setBlockState(structureWorldAccess, blockPos2, PrismaticIce.INSTANCE.getDefaultState());
                        } else {
                            this.setBlockState(structureWorldAccess, blockPos2, Blocks.PACKED_ICE.getDefaultState());
                        }
                        blockPos2 = blockPos2.down();
                        --p;
                        if (p <= 0) {
                            blockPos2 = blockPos2.down(random.nextInt(5) + 1);
                            p = random.nextInt(5);
                        }
                    }
                }
            }

            return true;
        }
    }

}
