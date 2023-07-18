package tf.bug.alymod.imixin;

import java.util.List;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface IPlayerEntityExtension extends IEntityExtension {

    List<Vec2f> getBaseVelocities();

}
