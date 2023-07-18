package tf.bug.alymod.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.ArrowEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.imixin.IPersistentProjectileEntityExtension;
import tf.bug.alymod.item.AmethystBolt;
import tf.bug.alymod.mixin.PersistentProjectileEntityAccessor;

public class AmethystBoltEntity extends PersistentProjectileEntity implements IPersistentProjectileEntityExtension {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "amethyst_bolt");

    public static final EntityType<AmethystBoltEntity> TYPE =
            FabricEntityTypeBuilder.<AmethystBoltEntity>create(SpawnGroup.MISC, AmethystBoltEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(20)
                    .build();

    public static final Identifier TEXTURE_ID =
            Identifier.of(Alymod.ID, "textures/entity/projectiles/amethyst_bolt.png");

    public AmethystBoltEntity(LivingEntity owner, World world) {
        super(AmethystBoltEntity.TYPE, owner, world);
        this.setDamage(3.0d);
    }

    private AmethystBoltEntity(EntityType<AmethystBoltEntity> etabe, World world) {
        super(etabe, world);
        this.setDamage(3.0d);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public float getDragInAir() {
        return 1.0f;
    }

    @Override
    protected float getDragInWater() {
        return 1.0f;
    }

    @Override
    public byte getPierceLevel() {
        return 0x7F;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity hit = entityHitResult.getEntity();
        float f = (float)this.getVelocity().length();
        int i = MathHelper.ceil(MathHelper.clamp((double)f * this.getDamage(), 0.0, 2.147483647E9));

        PersistentProjectileEntityAccessor thisx = (PersistentProjectileEntityAccessor) this;

        if (thisx.getPiercedEntities() == null) {
            thisx.setPiercedEntities(new IntOpenHashSet(5));
        }

        thisx.getPiercedEntities().add(hit.getId());

        if (this.isCritical()) {
            long l = (long)this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(l + (long)i, 2147483647L);
        }

        Entity owner = this.getOwner();
        DamageSource damageSource;
        if (owner == null) {
            damageSource = this.getDamageSources().arrow(this, this);
        } else {
            damageSource = this.getDamageSources().arrow(this, owner);
            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.onAttacking(hit);
            }
        }

        boolean hitEnderman = hit.getType() == EntityType.ENDERMAN;
        int j = hit.getFireTicks();
        if (this.isOnFire() && !hitEnderman) {
            hit.setOnFireFor(5);
        }

        if (hit.damage(damageSource, (float)i)) {
            if (hitEnderman) {
                return;
            }

            if (hit instanceof LivingEntity livingHit) {
                if (!this.getWorld().isClient && this.getPierceLevel() <= 0) {
                    livingHit.setStuckArrowCount(livingHit.getStuckArrowCount() + 1);
                }

                if (this.getPunch() > 0) {
                    double d = Math.max(0.0, 1.0 - livingHit.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                    Vec3d vec3d = this.getVelocity().multiply(1.0, 0.0, 1.0).normalize().multiply((double)this.getPunch() * 0.6 * d);
                    if (vec3d.lengthSquared() > 0.0) {
                        livingHit.addVelocity(vec3d.x, 0.1, vec3d.z);
                    }
                }

                if (!this.getWorld().isClient && owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.onUserDamaged(livingHit, livingOwner);
                    EnchantmentHelper.onTargetDamaged(livingOwner, livingHit);
                }

                this.onHit(livingHit);

                if (livingHit != owner && livingHit instanceof PlayerEntity && owner instanceof ServerPlayerEntity playerOwner && !this.isSilent()) {
                    playerOwner.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, 0.0F));
                }
            }

            this.playSound(this.getSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        } else {
            hit.setFireTicks(j);
            this.setVelocity(this.getVelocity().multiply(-0.1));
            this.setYaw(this.getYaw() + 180.0F);
            this.prevYaw += 180.0F;
            if (!this.getWorld().isClient && this.getVelocity().lengthSquared() < 1.0E-7) {
                if (this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1F);
                }

                this.discard();
            }
        }
    }

    @Override
    protected ItemStack asItemStack() {
        return new ItemStack(AmethystBolt.INSTANCE);
    }

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE, AmethystBoltEntity.ID, AmethystBoltEntity.TYPE);
    }

    @Environment(EnvType.CLIENT)
    public static class Renderer extends ProjectileEntityRenderer<AmethystBoltEntity> {
        public Renderer(EntityRendererFactory.Context context) {
            super(context);
        }

        @Override
        public Identifier getTexture(AmethystBoltEntity entity) {
            return AmethystBoltEntity.TEXTURE_ID;
        }
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        EntityRendererRegistry.register(AmethystBoltEntity.TYPE, AmethystBoltEntity.Renderer::new);
    }

}
