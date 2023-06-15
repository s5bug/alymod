package tf.bug.alymod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.bug.alymod.advancement.InteractPrismaticIceMerchantCriterion;
import tf.bug.alymod.block.PrismaticIce;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "sendTradeOffers")
    public void checkOffersForPrismaticIce(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable, CallbackInfo ci) {
        ServerPlayerEntity thisx = (ServerPlayerEntity) (Object) this;
        boolean offersIce = offers.stream()
                .anyMatch(to -> to.getSellItem().isOf(PrismaticIce.ITEM_INSTANCE));

        if (offersIce) {
            InteractPrismaticIceMerchantCriterion.INSTANCE.trigger(thisx);
        }
    }

}
