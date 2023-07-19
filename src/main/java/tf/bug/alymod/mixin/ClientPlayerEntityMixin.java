package tf.bug.alymod.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.item.EclipticClaw;
import tf.bug.alymod.network.ImpulseJumpMessage;

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
                        .multiply(EclipticClaw.additionalImpulseSpeed(currentVelocity.length()) + currentVelocity.length());
                if(thisx.getPitch() < 15.0f) {
                    // ramp jump up from 15 to -15 degrees
                    float factor = ((15.0f - thisx.getPitch()) * (3.0f)) / 90.0f;
                    // if we're falling then we don't want to get much higher
                    if(currentVelocity.y < 0)
                        factor /= 1.0f + (-currentVelocity.y);

                    if(factor > 1.0f)
                        factor = 1.0f;

                    // prevent too much speed gain when going horizontal
                    factor *= factor;

                    float jv = ((LivingEntityAccessor) thisx).invokeGetJumpVelocity();

                    Vec3d jump = new Vec3d(0.0d, jv * factor, 0.0d);
                    thisx.setVelocity(redirect.add(jump));
                } else {
                    thisx.setVelocity(redirect);
                }

                thisx.playSound(EclipticClaw.IMPULSE_SOUND_EVENT, SoundCategory.PLAYERS, EclipticClaw.IMPULSE_SOUND_VOLUME, 1.0f);
                ClientPlayNetworking.send(new ImpulseJumpMessage(thisx.getUuid()));
            }
        }
    }

}
