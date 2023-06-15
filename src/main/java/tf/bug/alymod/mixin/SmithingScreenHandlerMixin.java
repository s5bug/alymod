package tf.bug.alymod.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ForgingScreenHandler {

    private SmithingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Shadow protected abstract void decrementStack(int slot);

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/SmithingScreenHandler;decrementStack(I)V"
            ),
            method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V"
    )
    public void redirectTemplateDecrement(SmithingScreenHandler instance, int slot) {
        ItemStack stack = instance.getSlot(slot).getStack();
        if(stack != null) {
            if (!stack.isEmpty() && stack.getItem().hasRecipeRemainder()) {
                this.input.setStack(slot, stack.getItem().getRecipeRemainder(stack));
            } else {
                this.decrementStack(slot);
            }
        }
    }

}
