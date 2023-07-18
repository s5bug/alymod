package tf.bug.alymod.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.imixin.IEntityExtension;
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.mixin.EntityAccessor;
import tf.bug.alymod.mixin.LivingEntityAccessor;
import tf.bug.alymod.mixin.PlayerEntityMixin;

public class EclipticClaw extends Item {

    private EclipticClaw(Settings settings) {
        super(settings);
    }

    public static Item.Settings SETTINGS =
            new FabricItemSettings()
                    .maxCount(1);

    public static Identifier ID =
            Identifier.of(Alymod.ID, "ecliptic_claw");

    public static EclipticClaw INSTANCE =
            new EclipticClaw(EclipticClaw.SETTINGS);

    public static void register() {
        Registry.register(Registries.ITEM, EclipticClaw.ID, EclipticClaw.INSTANCE);
    }

    // Movement functions
    public static void beforeTick(PlayerEntity player) {
        if(!player.getWorld().isClient()) return;

        if(!((IPlayerEntityExtension) player).getBaseVelocities().isEmpty()) {
            ((IPlayerEntityExtension) player).getBaseVelocities().clear();
        }
    }

    public static boolean overrideUpdateVelocity(Entity e, float speed, Vec3d movementInput) {
        if(e instanceof PlayerEntity player) {
            if(!player.getWorld().isClient()) return false;
            if(!player.getInventory().contains(EclipticClaw.INSTANCE.getDefaultStack())) return false;

            if(player.hasStatusEffect(StatusEffects.LEVITATION)) return false;
            if((player.getAbilities().flying && player.getVehicle() == null) || player.isTouchingWater() || player.isInLava() || player.isClimbing()) return false;

            speed *= 2.15f;
            Vec2f direction = getMovementDirection(player, new Vec2f((float) movementInput.x, (float) movementInput.z));
            ((IPlayerEntityExtension) player).getBaseVelocities().add(direction.multiply(speed));

            return true;
        } else {
            return false;
        }
    }

    public static boolean overrideTravel(PlayerEntity player, Vec3d movementInput) {
        if(!player.getWorld().isClient()) return false;
        if(!player.getInventory().contains(EclipticClaw.INSTANCE.getDefaultStack())) return false;

        if(player.hasStatusEffect(StatusEffects.LEVITATION)) return false;
        // todo shouldReturnMovement
        if((player.getAbilities().flying || player.isFallFlying()) && player.getVehicle() == null)
            return false;

        double x1 = player.getX();
        double y1 = player.getY();
        double z1 = player.getZ();
        if(Quake.travel(player, movementInput)) {
            player.increaseTravelMotionStats(player.getX() - x1, player.getY() - y1, player.getZ() - z1);
            return true;
        } else {
            return false;
        }
    }

    public static void afterJump(PlayerEntity player) {
        if(!player.getWorld().isClient()) return;
        if(!player.getInventory().contains(EclipticClaw.INSTANCE.getDefaultStack())) return;

        if(player.hasStatusEffect(StatusEffects.LEVITATION)) return;

        if(player.isSprinting()) {
            float f = player.getYaw() * 0.017453292F;

            Vec3d v = player.getVelocity();

            double x = v.x + (MathHelper.sin(f) * 0.2F);
            double z = v.z - (MathHelper.cos(f) * 0.2F);

            player.setVelocity(x, v.y, z);
        }

        Quake.jump(player);
    }

    public static Vec2f getMovementDirection(PlayerEntity player, Vec2f movementInput) {
        float x = 0.0f;
        float z = 0.0f;

        float f3 = movementInput.lengthSquared();
        if(f3 >= 0.0001f) {
            float l = MathHelper.sqrt(f3);
            if(l < 1.0f) l = 1.0f;

            float il = 1.0f / l;

            float xm = movementInput.x * il;
            float zm = movementInput.y * il;

            float f4 = MathHelper.sin(player.getYaw() * (float) Math.PI / 180.0f);
            float f5 = MathHelper.cos(player.getYaw() * (float) Math.PI / 180.0f);
            x = (xm * f5) - (zm * f4);
            z = (zm * f5) + (xm * f4);
        }

        return new Vec2f(x, z);
    }

    public static class Quake {

        public static final boolean UNCAPPED_BUNNYHOP = true;
        public static final double AIR_ACCELERATE = 14.0D;
        public static final double MAX_AIR_ACCEL_PER_TICK = 0.045D;
        public static final double ACCELERATE = 10.0D;
        public static final double SOFT_CAP = (1.4D) * 0.125D;
        public static final double HARD_CAP = (2.0D) * 0.125D;
        public static final double SOFT_CAP_DEGEN = 0.65D;
        public static final double SHARKING_SURFACE_TENSION = 0.2D;
        public static final double SHARKING_WATER_FRICTION = 1.0D - (0.1D) * 0.05D;
        public static final double TRIMP_MULTIPLIER = 1.4D;

        public static void swingLimbsBasedOnMovement(PlayerEntity player) {
            player.updateLimbs(player instanceof Flutterer);
        }

        public static boolean travel(PlayerEntity player, Vec3d movementInput) {
            if(player.isClimbing()) {
                return false;
            } else if((player.isInLava() && !player.getAbilities().flying)) {
                return false;
            } else if(player.isTouchingWater() && !player.getAbilities().flying) {
                Quake.shark(player, movementInput);
            } else {
                Vec2f dir = new Vec2f((float) movementInput.x, (float) movementInput.z);
                float speed = dir.equals(Vec2f.ZERO) ? 0.0f : Quake.movementSpeed(player);
                Vec2f moveDir = EclipticClaw.getMovementDirection(player, dir);
                boolean fullyOnGround = player.isOnGround() && !((LivingEntityAccessor) player).getJumping();
                float momentumRetention = Quake.slipperiness(player);

                if(fullyOnGround) {
                    // apply friction using momentumRetention
                    Vec3d v = player.getVelocity();
                    player.setVelocity(v.x * momentumRetention, v.y, v.z * momentumRetention);

                    double accel = Quake.ACCELERATE;

                    if(speed != 0.0F) {
                        double momentumRetention3 = momentumRetention * momentumRetention * momentumRetention;
                        double mcSpeed = player.getMovementSpeed() * (0.16277136F / momentumRetention3);
                        accel *= mcSpeed * 2.15F / speed;

                        Quake.accelerate(player, speed, moveDir, accel);
                    }

                    if(!((IPlayerEntityExtension) player).getBaseVelocities().isEmpty()) {
                        float speedMod = speed / Quake.maxMovementSpeed(player);

                        Vec3d vi = player.getVelocity();
                        Vec2f motions = new Vec2f((float) vi.x, (float) vi.z);
                        for(Vec2f velocity : ((IPlayerEntityExtension) player).getBaseVelocities()) {
                            motions = motions.add(velocity.multiply(speedMod));
                        }

                        player.setVelocity(motions.x, vi.y, motions.y);
                    }
                } else {
                    Quake.airAccelerate(player, speed, moveDir, Quake.AIR_ACCELERATE);

                    if(Quake.SHARKING_SURFACE_TENSION > 0.0D && ((LivingEntityAccessor) player).getJumping() && player.getVelocity().y < 0.0D) {
                        Box b = player.getBoundingBox().offset(player.getVelocity());
                        boolean isFallingIntoWater = player.getWorld().containsFluid(b);

                        if(isFallingIntoWater) {
                            Vec3d vi = player.getVelocity();
                            player.setVelocity(vi.x, vi.y * Quake.SHARKING_SURFACE_TENSION, vi.z);
                        }
                    }
                }

                player.move(MovementType.SELF, player.getVelocity());

                Quake.applyGravity(player);
            }

            Quake.swingLimbsBasedOnMovement(player);

            return true;
        }

        public static void jump(PlayerEntity player) {
            Quake.applySoftCap(player, Quake.maxMovementSpeed(player));

            boolean didTrimp = Quake.doTrimp(player);

            if(!didTrimp) {
                Quake.applyHardCap(player, Quake.maxMovementSpeed(player));
            }
        }

        public static boolean doTrimp(PlayerEntity player) {
            if(player.isSneaking()) {
                Vec3d v = player.getVelocity();
                float currentSpeed = new Vec2f((float) v.x, (float) v.z).length();
                float movementSpeed = Quake.maxMovementSpeed(player);

                if(currentSpeed > movementSpeed) {
                    double speedBonus = currentSpeed / movementSpeed * 0.5F;
                    if(speedBonus > 1.0F)
                        speedBonus = 1.0F;

                    double y = v.y + speedBonus * currentSpeed * Quake.TRIMP_MULTIPLIER;
                    player.setVelocity(v.x, y, v.z);

                    if(Quake.TRIMP_MULTIPLIER > 0) {
                        float mult = (float) (1.0f / Quake.TRIMP_MULTIPLIER);
                        v = player.getVelocity();
                        player.setVelocity(v.x * mult, v.y, v.z * mult);
                    }

                    // TODO spawn particles

                    return true;
                }
            }
            return false;
        }

        public static float movementSpeed(PlayerEntity player) {
            return player.isSneaking() ? player.getMovementSpeed() * 1.11F : player.getMovementSpeed() * 2.15F;
        }

        public static float maxMovementSpeed(PlayerEntity player) {
            return player.getMovementSpeed() * 2.15F;
        }

        public static float slipperiness(PlayerEntity player) {
            BlockPos velocityAffectingPos = ((EntityAccessor) player).invokeGetVelocityAffectingPos();
            float slipperiness = player.getWorld().getBlockState(velocityAffectingPos).getBlock().getSlipperiness();
            return player.isOnGround() ? slipperiness * 0.91F : 0.91F;
        }

        public static void shark(PlayerEntity player, Vec3d movementInput) {
            double posY = player.getY();
            Vec3d v = player.getVelocity();

            Vec2f dir = new Vec2f((float) movementInput.x, (float) movementInput.z);
            float speed = dir.equals(Vec2f.ZERO) ? 0.0f : Quake.movementSpeed(player);
            Vec2f moveDir = EclipticClaw.getMovementDirection(player, dir);
            boolean isSharking = ((LivingEntityAccessor) player).getJumping() && Quake.isOffsetPositionInLiquid(player, 0.0d, 1.0d, 0.0d);
            float movementSpeed = new Vec2f((float) v.x, (float) v.z).length();

            if(!isSharking || movementSpeed < 0.078F) {
                Quake.waterMove(player, movementInput);
            } else {
                if(movementSpeed > 0.09F)
                    Quake.applyWaterFriction(player, Quake.SHARKING_WATER_FRICTION);

                if(movementSpeed > 0.098F)
                    Quake.airAccelerate(player, speed, moveDir, Quake.ACCELERATE);
                else
                    Quake.accelerate(player, .0980F, moveDir, Quake.ACCELERATE);

                player.move(MovementType.SELF, player.getVelocity());

                Vec3d vi = player.getVelocity();
                player.setVelocity(vi.x, 0, vi.z);
            }

            v = player.getVelocity();
            if(player.horizontalCollision && Quake.isOffsetPositionInLiquid(player, v.x, v.y + 0.6000000238418579D - player.getY() + posY, v.z)) {
                player.setVelocity(v.x, 0.30000001192092896D, v.z);
            }

            if(!((IPlayerEntityExtension) player).getBaseVelocities().isEmpty()) {
                float speedMod = speed / Quake.maxMovementSpeed(player);

                Vec3d vi = player.getVelocity();
                Vec2f motions = new Vec2f((float) vi.x, (float) vi.z);
                for(Vec2f velocity : ((IPlayerEntityExtension) player).getBaseVelocities()) {
                    motions = motions.add(velocity.multiply(speedMod));
                }

                player.setVelocity(motions.x, vi.y, motions.y);
            }
        }

        public static boolean isOffsetPositionInLiquid(PlayerEntity player, double x, double y, double z) {
            Box b = player.getBoundingBox().offset(x, y, z);
            return isLiquidPresentInBox(player, b);
        }

        public static boolean isLiquidPresentInBox(PlayerEntity player, Box b) {
            return player.getWorld().isSpaceEmpty(player, b) && !player.getWorld().containsFluid(b);
        }

        public static void waterMove(PlayerEntity player, Vec3d movementInput) {
            double y0 = player.getY();
            player.updateVelocity(0.04F, movementInput);

            Vec3d v = player.getVelocity();
            player.move(MovementType.SELF, v);

            double x = v.x * 0.800000011920929D;
            double y = v.y * 0.800000011920929D;
            double z = v.z * 0.800000011920929D;
            y -= 0.02D;

            player.setVelocity(x, y, z);
            v = player.getVelocity();

            if(player.horizontalCollision && Quake.isOffsetPositionInLiquid(player, x, y + 0.6000000238418579D - player.getY() + y0, z)) {
                player.setVelocity(v.x, 0.30000001192092896D, v.z);
            }
        }

        public static void applyWaterFriction(PlayerEntity player, double friction) {
            player.setVelocity(player.getVelocity().multiply(friction));
        }

        public static void accelerate(PlayerEntity player, float speed, Vec2f dir, double accel) {
            Vec3d v = player.getVelocity();
            Vec2f v2 = new Vec2f((float) v.x, (float) v.z);

            float currentSpeed = v2.dot(dir);

            float addSpeed = speed - currentSpeed;

            if(addSpeed <= 0.0f) return;

            double accelSpeed = accel * speed / Quake.slipperiness(player) * 0.05F;

            if(accelSpeed > addSpeed)
                accelSpeed = addSpeed;

            player.setVelocity(v.x + (accelSpeed * dir.x), v.y, v.z + (accelSpeed * dir.y));
        }

        public static void airAccelerate(PlayerEntity player, float speed, Vec2f dir, double accel) {
            float wSpeed = speed;
            if(wSpeed > MAX_AIR_ACCEL_PER_TICK)
                wSpeed = (float) MAX_AIR_ACCEL_PER_TICK;

            Vec3d v = player.getVelocity();
            Vec2f v2 = new Vec2f((float) v.x, (float) v.z);

            float currentSpeed = v2.dot(dir);

            float addSpeed = wSpeed - currentSpeed;

            if(addSpeed <= 0.0f) return;

            double accelSpeed = accel * speed * 0.05F;

            if(accelSpeed > addSpeed)
                accelSpeed = addSpeed;

            player.setVelocity(v.x + (accelSpeed * dir.x), v.y, v.z + (accelSpeed * dir.y));
        }

        public static void applySoftCap(PlayerEntity player, float moveSpeed) {
            float scPct = (float) Quake.SOFT_CAP;
            float scDeg = (float) Quake.SOFT_CAP_DEGEN;

            if(Quake.UNCAPPED_BUNNYHOP) {
                scPct = 1.0f;
                scDeg = 1.0f;
            }

            Vec3d v = player.getVelocity();
            float speed = new Vec2f((float) v.x, (float) v.z).length();
            float softCap = moveSpeed * scPct;

            if(speed > softCap) {
                if(scDeg != 1.0F) {
                    float appliedCap = (speed - softCap) * scDeg + softCap;
                    float mult = appliedCap / speed;

                    player.setVelocity(v.x * mult, v.y, v.z * mult);
                }

                // todo spawn particles
            }
        }

        public static void applyHardCap(PlayerEntity player, float moveSpeed) {
            if(Quake.UNCAPPED_BUNNYHOP)
                return;

            float hardCapPercent = (float) Quake.HARD_CAP;

            Vec3d v = player.getVelocity();
            float speed = new Vec2f((float) v.x, (float) v.z).length();
            float hardCap = moveSpeed * hardCapPercent;

            if(speed > hardCap && hardCap != 0.0F) {
                float mult = hardCap / speed;

                player.setVelocity(v.x * mult, v.y, v.z * mult);

                // TODO spawn particles
            }
        }

        public static void applyGravity(PlayerEntity player) {
            if (player.hasNoGravity()) return;

            Vec3d v = player.getVelocity();
            double yv = v.y;

            if(player.getWorld().isClient() && !player.getWorld().isChunkLoaded(((EntityAccessor) player).invokeGetVelocityAffectingPos())) {
                if(player.getY() > player.getWorld().getBottomY()) {
                    yv = -0.1d;
                } else {
                    yv = -0.0d;
                }
            } else {
                double gravity = 0.08d;
                boolean falling = v.y <= 0.0d;
                if (falling && player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    gravity = 0.01d;
                }

                yv -= gravity;
            }

            yv *= 0.9800000190734863D;

            player.setVelocity(v.x, yv, v.z);
        }

    }

}
