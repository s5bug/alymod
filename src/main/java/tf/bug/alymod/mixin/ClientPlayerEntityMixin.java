package tf.bug.alymod.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.item.EclipticClaw;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"
            ),
            method = "tickMovement()V"
    )
    public void checkDoubleJump(CallbackInfo ci) {
        ClientPlayerEntity thisx = (ClientPlayerEntity) (Object) this;

        if(!thisx.isOnGround() && thisx.getInventory().contains(EclipticClaw.INSTANCE.getDefaultStack())) {
            boolean canDoubleJump = ((IPlayerEntityExtension) thisx).useEclipticClawImpulse();
            if(canDoubleJump) {
                Vec3d currentVelocity = thisx.getVelocity();
                Vec3d look = thisx.getRotationVector();

                Vec3d redirect = look.normalize()
                        .multiply(EclipticClaw.ADDITIONAL_IMPULSE_VELOCITY + currentVelocity.length());
                thisx.setVelocity(redirect);
            }
        }
    }

}
