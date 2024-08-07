package tf.bug.alymod.attachment;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class PlayerMonkAttachments {

    public static Identifier GREASED_LIGHTNING_ID =
            Identifier.of(Alymod.ID, "greased_lightning");

    public static Supplier<AttachmentType<Integer>> GREASED_LIGHTNING =
            Suppliers.memoize(() -> AttachmentRegistry.createDefaulted(
                    PlayerMonkAttachments.GREASED_LIGHTNING_ID,
                    () -> 0
            ));
    public static AttachmentType<Integer> greasedLightning() {
        return GREASED_LIGHTNING.get();
    }

    public static Identifier GREASED_LIGHTNING_EXPIRES_ID =
            Identifier.of(Alymod.ID, "greased_lightning_expires");

    public static Supplier<AttachmentType<Long>> GREASED_LIGHTNING_EXPIRES =
            Suppliers.memoize(() -> AttachmentRegistry.create(
                    PlayerMonkAttachments.GREASED_LIGHTNING_EXPIRES_ID
            ));
    public static AttachmentType<Long> greasedLightningExpires() {
        return GREASED_LIGHTNING_EXPIRES.get();
    }

    public static Identifier CHAKRA_ID =
            Identifier.of(Alymod.ID, "chakra");

    public static Supplier<AttachmentType<Integer>> CHAKRA =
            Suppliers.memoize(() -> AttachmentRegistry.createDefaulted(
                    PlayerMonkAttachments.CHAKRA_ID,
                    () -> 0
            ));
    public static AttachmentType<Integer> chakra() {
        return CHAKRA.get();
    }

    public static void register() {
        PlayerMonkAttachments.greasedLightning();
        PlayerMonkAttachments.greasedLightningExpires();
        PlayerMonkAttachments.chakra();
    }

}
