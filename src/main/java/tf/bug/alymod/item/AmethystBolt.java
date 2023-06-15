package tf.bug.alymod.item;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.impl.client.model.ModelLoaderHooks;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.entity.AmethystBoltEntity;

public class AmethystBolt extends Item {
    private AmethystBolt(Settings settings) {
        super(settings);
    }

    public static final Item.Settings SETTINGS =
            new FabricItemSettings();

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "amethyst_bolt");

    public static final AmethystBolt INSTANCE =
            new AmethystBolt(SETTINGS);

    public static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated) {
        AmethystBoltEntity pje = new AmethystBoltEntity(shooter, world);

        if(creative || simulated != 0.0F) {
            pje.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        }

        if(shooter instanceof CrossbowUser crossbowUser) {
            crossbowUser.shoot(crossbowUser.getTarget(), crossbow, pje, simulated);
        } else {
            Vec3d vec3d = shooter.getOppositeRotationVector(1.0F);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis(simulated * 0.017453292F, vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = shooter.getRotationVec(1.0F);
            Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
            pje.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
        }

        crossbow.damage(2, shooter, (e) -> {
            e.sendToolBreakStatus(hand);
        });

        world.spawnEntity(pje);
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, soundPitch);
    }

    public static void register() {
        Registry.register(Registries.ITEM, AmethystBolt.ID, AmethystBolt.INSTANCE);
    }

    public static final Identifier CROSSBOW_MODEL_ID =
            Identifier.of(Alymod.ID, "item/amethyst_bolt_crossbow");

    public static final List<ModelOverride.Condition> CROSSBOW_MODEL_CONDITIONS =
            List.of(
                    new ModelOverride.Condition(new Identifier("charged"), 1.0f),
                    new ModelOverride.Condition(AmethystBolt.ID, 1.0f)
            );

    public static void registerClient() {
        ModelPredicateProviderRegistry.register(Items.CROSSBOW, AmethystBolt.ID, (stack, world, entity, seed) -> {
            return CrossbowItem.isCharged(stack) && CrossbowItem.hasProjectile(stack, AmethystBolt.INSTANCE) ? 1.0F : 0.0F;
        });


    }

}
