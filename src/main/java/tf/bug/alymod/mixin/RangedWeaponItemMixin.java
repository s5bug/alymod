package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {

    private static final TagKey<Item> CROSSBOW_PROJECTILES_TAG =
            TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "crossbow_projectiles"));

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Predicate;or(Ljava/util/function/Predicate;)Ljava/util/function/Predicate;"
            ),
            method = "<clinit>"
    )
    private static Predicate<ItemStack> hijackCrossbowProjectilesPredicate(Predicate<ItemStack> other) {
        return other.or((stack) -> stack.isIn(CROSSBOW_PROJECTILES_TAG));
    }

}
