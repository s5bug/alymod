package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.item.MonkSoul;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "renderHotbar",
            cancellable = true
    )
    private void renderMonkHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        InGameHud thisx = (InGameHud) (Object) this;

        if(MonkSoul.Client.renderHotbar(thisx, context, tickCounter))
            ci.cancel();
    }

    @ModifyExpressionValue(
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=32"
            ),
            method = "renderMountJumpBar"
    )
    private int adjustJumpBarHeight(int value) {
        InGameHud thisx = (InGameHud) (Object) this;

        if(MonkSoul.Client.shouldRenderHotbar(thisx))
            return value + 60;
        else return value;
    }

    @ModifyExpressionValue(
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=32"
            ),
            method = "renderExperienceBar"
    )
    private int adjustExperienceBarHeight(int value) {
        InGameHud thisx = (InGameHud) (Object) this;

        if(MonkSoul.Client.shouldRenderHotbar(thisx))
            return value + 60;
        else return value;
    }

    @ModifyExpressionValue(
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=39"
            ),
            method = "renderStatusBars"
    )
    private int adjustStatusBarsHeight(int value) {
        InGameHud thisx = (InGameHud) (Object) this;

        if(MonkSoul.Client.shouldRenderHotbar(thisx))
            return value + 60;
        else return value;
    }

}
