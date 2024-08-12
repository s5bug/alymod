package tf.bug.alymod.monk;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import tf.bug.alymod.ConeBoxIntersection;

public interface TargetStrategy<T extends Entity> {

    public static final record Result<T extends Entity>(boolean castSucceeded, List<T> targets) {}

    @Environment(EnvType.CLIENT)
    Result<T> attemptTarget(ClientPlayerEntity player, float tickDelta);

    public static final record Self() implements TargetStrategy<PlayerEntity> {
        @Environment(EnvType.CLIENT)
        @Override
        public Result<PlayerEntity> attemptTarget(ClientPlayerEntity player, float tickDelta) {
            return new Result<>(true, List.of(player));
        }
    }

    public static final record Single(int range) implements TargetStrategy<Entity> {
        @Environment(EnvType.CLIENT)
        @Override
        public Result<Entity> attemptTarget(ClientPlayerEntity player, float tickDelta) {
            Vec3d segment = player.getRotationVec(tickDelta).multiply(range);
            Vec3d from = player.getCameraPosVec(tickDelta);
            Vec3d to = from.add(segment);
            Box box = player.getBoundingBox().stretch(segment).expand(1.0);

            EntityHitResult res = ProjectileUtil.raycast(
                    player,
                    from,
                    to,
                    box,
                    e -> !e.isSpectator() && e.canHit(),
                    range * range // of course
            );
            if(res != null) {
                return new Result<>(true, List.of(res.getEntity()));
            } else return new Result<>(false, List.of());
        }
    }

    public static final record GapClose(int range) implements TargetStrategy<Entity> {
        private static double rayIntersectsBox(Box box, Vec3d ray) {
            if(box.contains(Vec3d.ZERO)) return 0.0d;

            double dfx = 1.0d / ray.x;
            double dfy = 1.0d / ray.y;
            double dfz = 1.0d / ray.z;

            double t1 = box.minX * dfx;
            double t2 = box.maxX * dfx;
            double t3 = box.minY * dfy;
            double t4 = box.maxY * dfy;
            double t5 = box.minZ * dfz;
            double t6 = box.maxZ * dfz;

            double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
            double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

            if(tmax < 0) return -1;
            if(tmin > tmax) return -1;

            return tmin;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public Result<Entity> attemptTarget(ClientPlayerEntity player, float tickDelta) {
            Vec3d ray = player.getRotationVec(tickDelta);
            Vec3d segment = ray.multiply(range);
            Vec3d from = player.getCameraPosVec(tickDelta);
            Vec3d to = from.add(segment);
            Box box = player.getBoundingBox().stretch(segment).expand(1.0);

            EntityHitResult res = ProjectileUtil.raycast(
                    player,
                    from,
                    to,
                    box,
                    e -> !e.isSpectator() && e.canHit(),
                    range * range // of course
            );

            if(res != null) {
                Entity e = res.getEntity();

                double rayLength = rayIntersectsBox(
                        e.getBoundingBox().offset(from.negate()),
                        ray
                );

                if(rayLength >= 0.0d) {
                    player.move(MovementType.SELF, ray.multiply(Math.max(0, rayLength - 1.5d)));

                    return new Result<>(true, List.of(e));
                } else return new Result<>(false, List.of());
            } else return new Result<>(false, List.of());
        }
    }

    public static final record Spherical(int radius) implements TargetStrategy<Entity> {
        @Environment(EnvType.CLIENT)
        @Override
        public Result<Entity> attemptTarget(ClientPlayerEntity player, float tickDelta) {
            List<Entity> targets = player.getWorld().getOtherEntities(
                    player,
                    player.getBoundingBox().expand(radius),
                    e -> !e.isSpectator() && e.canHit() &&
                            player.squaredDistanceTo(e) < (radius * radius));
            return new Result<>(true, targets);
        }
    }

    public static final record Conal(int radius, double angle) implements TargetStrategy<Entity> {
        @Environment(EnvType.CLIENT)
        @Override
        public Result<Entity> attemptTarget(ClientPlayerEntity player, float tickDelta) {
            Vec3d coneOrigin = player.getCameraPosVec(tickDelta);
            Vec3d coneDirection = player.getRotationVec(tickDelta);

            List<Entity> targets = player.getWorld().getOtherEntities(
                    player,
                    player.getBoundingBox().expand(radius),
                    e -> !e.isSpectator() && e.canHit() &&
                            ConeBoxIntersection.intersects(
                                    e.getBoundingBox(),
                                    coneOrigin,
                                    coneDirection,
                                    radius,
                                    angle
                            ));
            return new Result<>(true, targets);
        }
    }

    public static final record Line(int range) implements TargetStrategy<Entity> {
        @Environment(EnvType.CLIENT)
        @Override
        public Result<Entity> attemptTarget(ClientPlayerEntity player, float tickDelta) {
            Vec3d raycastMin = player.getCameraPosVec(tickDelta);
            Vec3d raycastMax = raycastMin.add(player.getRotationVec(tickDelta).multiply(range));

            List<Entity> targets = player.getWorld().getOtherEntities(
                    player,
                    player.getBoundingBox().expand(range),
                    e -> !e.isSpectator() && e.canHit() && (e.getBoundingBox().contains(raycastMin) ||
                            e.getBoundingBox().raycast(raycastMin, raycastMax).isPresent())
            );
            return new Result<>(true, targets);
        }
    }

    public static final record Aggressors() implements TargetStrategy<MobEntity> {
        @Environment(EnvType.CLIENT)
        @Override
        public Result<MobEntity> attemptTarget(ClientPlayerEntity player, float tickDelta) {
            ArrayList<MobEntity> targets = new ArrayList<>();
            for (Entity e : player.clientWorld.getEntities()) {
                if(e instanceof MobEntity me) {
                    targets.add(me);
                }
            }
            return new Result<>(true, targets);
        }
    }

}
