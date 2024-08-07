package tf.bug.alymod.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import tf.bug.alymod.imixin.IPersistentProjectileEntityExtension;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityExtension implements IPersistentProjectileEntityExtension {

    @Override
    public float alymod$getDragInAir() {
        return 0.99f;
    }

    @ModifyConstant(
            constant = { @Constant(floatValue = 0.99f) },
            method = "tick()V"
    )
    public float useDragInAir(float oldConstant) {
        return this.alymod$getDragInAir();
    }

}
