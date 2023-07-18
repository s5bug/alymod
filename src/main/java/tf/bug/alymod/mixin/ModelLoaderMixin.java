package tf.bug.alymod.mixin;

import java.util.Map;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tf.bug.alymod.item.AmethystBolt;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 1
            ),
            method = "loadModelFromJson(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;"
    )
    public Object hackCrossbowOverrides(Map<Identifier, JsonUnbakedModel> instance, Object k, Identifier id) {
        JsonUnbakedModel original = instance.get((Identifier) k);

        if(original != null && "minecraft:item/crossbow".equals(id.toString())) {
            original.getOverrides().add(new ModelOverride(
                    AmethystBolt.CROSSBOW_MODEL_ID,
                    AmethystBolt.getCrossbowModelConditions()
            ));
        }

        return original;
    }

}
