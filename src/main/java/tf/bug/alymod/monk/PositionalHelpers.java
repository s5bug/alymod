package tf.bug.alymod.monk;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

public final class PositionalHelpers {
    private PositionalHelpers() {}

    public static boolean isRear(PlayerEntity p, Entity target) {
        float targetYaw = target.getYaw();
        Vec3d offset = p.getPos().subtract(target.getPos());
        float offsetYaw = (float) (MathHelper.atan2(offset.x, offset.z) * 180.0 / Math.PI);

        return MathHelper.angleBetween(targetYaw, offsetYaw) > 135.0F;
    }

    public static boolean isFlank(PlayerEntity p, Entity target) {
        float targetYaw = target.getYaw();
        Vec3d offset = p.getPos().subtract(target.getPos());
        float offsetYaw = (float) (MathHelper.atan2(offset.x, offset.z) * 180.0 / Math.PI);

        return MathHelper.angleBetween(targetYaw, offsetYaw) > 45.0F &&
                MathHelper.angleBetween(targetYaw, offsetYaw) <= 135.0F;
    }

}
