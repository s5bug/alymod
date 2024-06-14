package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.bug.alymod.entity.AmethystBoltEntity;
import tf.bug.alymod.item.AmethystBolt;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(
            at = @At("HEAD"),
            cancellable = true,
            method = "getSpeed(Lnet/minecraft/component/type/ChargedProjectilesComponent;)F"
    )
    private static void getSpeed(ChargedProjectilesComponent stack, CallbackInfoReturnable<Float> cir) {
        if(stack.getProjectiles().stream().map(ItemStack::getItem).allMatch(AmethystBolt.INSTANCE::equals)) {
            cir.setReturnValue(4.2F);
            cir.cancel();
            return;
        }
    }

    @Inject(
            at = @At(value = "HEAD"),
            cancellable = true,
            method = "createArrowEntity"
    )
    private void createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir) {
        if(projectileStack.isOf(AmethystBolt.INSTANCE)) {
            cir.setReturnValue(new AmethystBoltEntity(shooter, world, projectileStack, weaponStack));
            cir.cancel();
            return;
        }
    }

    @Inject(
            at = @At("HEAD"),
            cancellable = true,
            method = "shoot"
    )
    private void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, LivingEntity target, CallbackInfo ci) {
        if(AmethystBoltEntity.TYPE.equals(projectile.getType())) {
            AmethystBolt.shoot((CrossbowItem) (Object) this, shooter, projectile, index, speed, divergence, yaw, target);
            ci.cancel();
            return;
        }
    }

}
