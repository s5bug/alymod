package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tf.bug.alymod.item.MonkSoul;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @WrapOperation(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"
            ),
            method = "onKey"
    )
    public void interceptActionKeySet(InputUtil.Key key, boolean pressed, Operation<Void> original) {
        if(MonkSoul.Client.interceptActionKeySet(key, pressed)) {
            return;
        } else {
            original.call(key, pressed);
        }
    }

    @WrapOperation(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V"
            ),
            method = "onKey"
    )
    public void interceptActionKeyPressed(InputUtil.Key key, Operation<Void> original) {
        if(MonkSoul.Client.interceptActionKeyPressed(key)) {
            return;
        } else {
            original.call(key);
        }
    }

}
