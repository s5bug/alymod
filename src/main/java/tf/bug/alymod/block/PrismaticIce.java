package tf.bug.alymod.block;

import java.util.Arrays;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.advancement.HearPrismaticIceCriterion;

public class PrismaticIce extends Block {

    private PrismaticIce(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, final BlockPos pos, Random random) {
        if(!world.isClient) {
            final SoundEvent ev = PrismaticIce.SOUND_GLOW;
            final float volume = 1.0f;

            world.playSound(
                    null,
                    pos,
                    ev,
                    SoundCategory.BLOCKS,
                    volume,
                    1.0f
            );

            final double distance = ev.getDistanceToTravel(volume);

            world.getPlayers(p -> pos.getSquaredDistanceFromCenter(p.getX(), p.getY(), p.getZ()) < distance * distance)
                    .forEach(HearPrismaticIceCriterion.INSTANCE::trigger);
        }
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
            if (world.getDimension().ultrawarm()) {
                world.removeBlock(pos, false);
                return;
            }

            world.setBlockState(pos, PrismaticFluid.BLOCK.getDefaultState());
        }
    }

    public static final BlockSoundGroup SOUND_GROUP =
            BlockSoundGroup.GLASS;

    public static final Identifier SOUND_GLOW_ID =
            Identifier.of(Alymod.ID, "block.prismatic_ice.glow");

    public static final SoundEvent SOUND_GLOW =
            SoundEvent.of(SOUND_GLOW_ID, 48.0f);

    public static final AbstractBlock.Settings SETTINGS =
            FabricBlockSettings.create()
                    .hardness(1.5f)
                    .resistance(3_600_000f)
                    .slipperiness(0.999f)
                    .ticksRandomly()
                    .luminance(4)
                    .sounds(PrismaticIce.SOUND_GROUP);

    public static final PrismaticIce INSTANCE =
            new PrismaticIce(SETTINGS);

    public static final Item.Settings ITEM_SETTINGS =
            new FabricItemSettings();

    public static final BlockItem ITEM_INSTANCE =
            new BlockItem(INSTANCE, ITEM_SETTINGS);

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "prismatic_ice");

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, PrismaticIce.SOUND_GLOW_ID, PrismaticIce.SOUND_GLOW);
        Registry.register(Registries.BLOCK, PrismaticIce.ID, PrismaticIce.INSTANCE);
        Registry.register(Registries.ITEM, PrismaticIce.ID, PrismaticIce.ITEM_INSTANCE);

        // Add to high-end trades list
        final TradeOffers.Factory sellPrismaticIce =
                (entity, random) -> {
                    final ItemStack emeraldPrice = new ItemStack(Items.EMERALD, 20);
                    final ItemStack blueIceConversion = new ItemStack(Items.BLUE_ICE, 1);
                    final ItemStack prismaticIceOutput = new ItemStack(PrismaticIce.ITEM_INSTANCE, 1);
                    return new TradeOffer(
                            emeraldPrice,
                            blueIceConversion,
                            prismaticIceOutput,
                            3,
                            1,
                            0.05F
                    );
                };
        TradeOffers.WANDERING_TRADER_TRADES.compute(2, (k, a) -> {
            TradeOffers.Factory[] result = Arrays.copyOf(a, a.length + 1);
            result[a.length] = sellPrismaticIce;
            return result;
        });
    }

}
