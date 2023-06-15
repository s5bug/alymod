package tf.bug.alymod.item;

import java.util.List;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.mixin.ItemAccessor;

public class BoltSmithingTemplate extends SmithingTemplateItem {

    private BoltSmithingTemplate(Text appliesToText, Text ingredientsText, Text titleText, Text baseSlotDescriptionText, Text additionsSlotDescriptionText, List<Identifier> emptyBaseSlotTextures, List<Identifier> emptyAdditionsSlotTextures) {
        super(appliesToText, ingredientsText, titleText, baseSlotDescriptionText, additionsSlotDescriptionText, emptyBaseSlotTextures, emptyAdditionsSlotTextures);
        ((ItemAccessor) this).setMaxCount(1);
    }

    @Override
    public boolean hasRecipeRemainder() {
        return true;
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack;
    }

    public static final Text APPLIES_TO_TEXT =
            Text.translatable(Util.createTranslationKey(
                    "item",
                    Identifier.of(Alymod.ID, "bolt_smithing_template.applies_to")
            )).formatted(Formatting.BLUE);

    public static final Text INGREDIENTS_TEXT =
            Text.translatable(Util.createTranslationKey(
                    "item",
                    Identifier.of(Alymod.ID, "bolt_smithing_template.ingredients")
            )).formatted(Formatting.BLUE);

    public static final Text NOT_CONSUMED_WARNING_TEXT =
            Text.translatable(Util.createTranslationKey(
                    "item",
                    Identifier.of(Alymod.ID, "bolt_smithing_template.not_consumed")
            )).formatted(Formatting.RED);

    public static final Text TITLE_TEXT =
            Text.translatable(Util.createTranslationKey(
                    "item",
                    Identifier.of(Alymod.ID, "bolt_smithing_template.title")
            )).formatted(Formatting.GRAY);

    public static final Text BASE_SLOT_DESCRIPTION_TEXT =
            Text.translatable(Util.createTranslationKey(
                    "item",
                    Identifier.of(Alymod.ID, "bolt_smithing_template.base_slot_description")
            ));

    public static final Text ADDITIONS_SLOT_DESCRIPTION_TEXT =
            Text.translatable(Util.createTranslationKey(
                    "item",
                    Identifier.of(Alymod.ID, "bolt_smithing_template.additions_slot_description")
            ));

    public static final List<Identifier> EMPTY_BASE_SLOT_TEXTURES =
            List.of(
                    Identifier.of(Alymod.ID, "item/empty_slot_blaze_rod")
            );

    public static final List<Identifier> EMPTY_ADDITIONS_SLOT_TEXTURES =
            List.of(
                    Identifier.of(Alymod.ID, "item/empty_slot_amethyst_shard"),
                    Identifier.of(Alymod.ID, "item/empty_slot_prismatic_shard")
            );

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "bolt_smithing_template");

    public static final BoltSmithingTemplate INSTANCE =
            new BoltSmithingTemplate(
                    APPLIES_TO_TEXT,
                    INGREDIENTS_TEXT,
                    TITLE_TEXT,
                    BASE_SLOT_DESCRIPTION_TEXT,
                    ADDITIONS_SLOT_DESCRIPTION_TEXT,
                    EMPTY_BASE_SLOT_TEXTURES,
                    EMPTY_ADDITIONS_SLOT_TEXTURES
            );

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int titleIdx = tooltip.indexOf(TITLE_TEXT);
        tooltip.add(titleIdx + 1, NOT_CONSUMED_WARNING_TEXT);
    }

    private static final Identifier BURIED_TREASURE_LOOT_TABLE_ID =
            LootTables.BURIED_TREASURE_CHEST;

    public static void register() {
        Registry.register(Registries.ITEM, BoltSmithingTemplate.ID, BoltSmithingTemplate.INSTANCE);

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if(source.isBuiltin() && BURIED_TREASURE_LOOT_TABLE_ID.equals(id)) {
                LootPoolEntry ie = ItemEntry.builder(BoltSmithingTemplate.INSTANCE)
                        .build();

                LootPool.Builder poolBuilder = new LootPool.Builder()
                        .with(ie)
                        .rolls(UniformLootNumberProvider.create(0.0f, 1.0f))
                        .bonusRolls(ConstantLootNumberProvider.create(0.0f));

                tableBuilder.pool(poolBuilder);
            }
        });
    }

}
