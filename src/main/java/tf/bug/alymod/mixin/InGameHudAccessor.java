package tf.bug.alymod.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {

    @Invoker()
    PlayerEntity invokeGetCameraPlayer();

}
