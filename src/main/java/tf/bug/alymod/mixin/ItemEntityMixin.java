package tf.bug.alymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.item.PrismaticFluidBucket;
import tf.bug.alymod.item.PrismaticShard;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    private ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ItemEntity;isOnGround()Z",
                    ordinal = 0
            ),
            method = "tick()V"
    )
    public void convertOnTick(CallbackInfo ci) {
        ItemEntity thisx = (ItemEntity) (Object) this;

        ItemStack stack = thisx.getStack();

        if(!this.firstUpdate && this.isSubmergedIn(PrismaticFluid.TAG) && stack.isOf(Items.AMETHYST_SHARD)) {
            ItemStack asPrismaticShards = new ItemStack(PrismaticShard.INSTANCE, stack.getCount());
            thisx.setStack(asPrismaticShards);
        }
    }

}
