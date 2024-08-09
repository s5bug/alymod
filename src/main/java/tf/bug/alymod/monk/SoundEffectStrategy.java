package tf.bug.alymod.monk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.item.MonkSoul;

public interface SoundEffectStrategy {

    SoundEffect register(String baseId);

    public static final record CastTargetInstant(float castVolume, float targetVolume) implements SoundEffectStrategy {
        public static final record Effect(
                float castVolume,
                Identifier castIdentifier,
                SoundEvent castEvent,
                float targetVolume,
                Identifier targetIdentifier,
                SoundEvent targetEvent
        ) implements SoundEffect {
            @Environment(EnvType.CLIENT)
            @Override
            public void clientOnSnapshotSelf(ClientPlayerEntity cpe) {
                if(this.castVolume <= 0.0f) return;
                cpe.playSound(
                        this.castEvent,
                        this.castVolume,
                        1.0f
                );
            }

            @Environment(EnvType.CLIENT)
            @Override
            public void clientOnSnapshotTarget(ClientPlayerEntity cpe, Entity target) {
                if(this.targetVolume <= 0.0f) return;
                cpe.clientWorld.playSound(
                        cpe,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        this.targetEvent,
                        SoundCategory.PLAYERS,
                        this.targetVolume,
                        1.0f
                );
            }

            @Override
            public void serverOnSnapshotSelf(ServerPlayerEntity pe) {
                if(this.castVolume <= 0.0f) return;
                pe.getServerWorld().playSoundFromEntity(
                        pe,
                        pe,
                        this.castEvent,
                        SoundCategory.PLAYERS,
                        this.castVolume,
                        1.0f
                );
            }

            @Override
            public void serverOnSnapshotTarget(ServerPlayerEntity pe, Entity target) {
                if(this.targetVolume <= 0.0f) return;
                pe.getServerWorld().playSoundFromEntity(
                        pe,
                        target,
                        this.targetEvent,
                        SoundCategory.PLAYERS,
                        this.targetVolume,
                        1.0f
                );
            }
        }

        @Override
        public SoundEffect register(String baseId) {
            Identifier castIdentifier = null;
            SoundEvent castEvent = null;
            if(this.castVolume > 0.0f) {
                castIdentifier = Identifier.of(Alymod.ID, "item.monk_soul." + baseId + "_cast");
                castEvent = SoundEvent.of(castIdentifier);
                Registry.register(Registries.SOUND_EVENT, castIdentifier, castEvent);
            }

            Identifier targetIdentifier = null;
            SoundEvent targetEvent = null;
            if(this.targetVolume > 0.0f) {
                targetIdentifier = Identifier.of(Alymod.ID, "item.monk_soul." + baseId + "_target");
                targetEvent = SoundEvent.of(targetIdentifier);
                Registry.register(Registries.SOUND_EVENT, targetIdentifier, targetEvent);
            }

            return new Effect(this.castVolume, castIdentifier, castEvent,
                    this.targetVolume, targetIdentifier, targetEvent);
        }
    }

}
