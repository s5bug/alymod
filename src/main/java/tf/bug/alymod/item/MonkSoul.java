package tf.bug.alymod.item;

import com.mojang.blaze3d.systems.RenderSystem;
import java.time.Duration;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.attachment.PlayerMonkAttachments;
import tf.bug.alymod.imixin.IPlayerEntityExtension;
import tf.bug.alymod.mixin.InGameHudAccessor;
import tf.bug.alymod.monk.MonkAction;
import tf.bug.alymod.monk.StatusHelpers;

public class MonkSoul extends Item {
    private MonkSoul(Settings settings) {
        super(settings);
    }

    public static Item.Settings SETTINGS = new Item.Settings()
            .maxCount(1)
            .fireproof()
            .rarity(Rarity.RARE);

    public static Identifier ID =
            Identifier.of(Alymod.ID, "monk_soul");

    public static MonkSoul INSTANCE =
            new MonkSoul(SETTINGS);

    @Environment(EnvType.CLIENT)
    public static final class Client {
        private Client() {}

        public static final Identifier HOTBAR_3X4_TEXTURE =
                Identifier.of(Alymod.ID, "hud/hotbar3x4");

        public static final Identifier HOTBAR_1X4_TEXTURE =
                Identifier.of(Alymod.ID, "hud/hotbar1x4");

        public static final Identifier[] HIGHLIGHT_TEXTURES = new Identifier[] {
                Identifier.of(Alymod.ID, "hud/highlight0"),
                Identifier.of(Alymod.ID, "hud/highlight1"),
                Identifier.of(Alymod.ID, "hud/highlight2"),
                Identifier.of(Alymod.ID, "hud/highlight3"),
                Identifier.of(Alymod.ID, "hud/highlight4"),
                Identifier.of(Alymod.ID, "hud/highlight5"),
                Identifier.of(Alymod.ID, "hud/highlight6"),
                Identifier.of(Alymod.ID, "hud/highlight7")
        };

        public static final Identifier GL_GAUGE =
                Identifier.of(Alymod.ID, "hud/gl_gauge");

        public static final Identifier GL_STACK =
                Identifier.of(Alymod.ID, "hud/gl_stack");

        public static final Identifier CHAKRA_GAUGE =
                Identifier.of(Alymod.ID, "hud/chakra_gauge");

        public static final Identifier CHAKRA_STACK =
                Identifier.of(Alymod.ID, "hud/chakra_stack");

        private static final MonkAction[][] hotbar1 = new MonkAction[][] {
                new MonkAction[] { MonkAction.BOOTSHINE, MonkAction.TRUE_STRIKE, MonkAction.DEMOLISH },
                new MonkAction[] { MonkAction.DRAGON_KICK, MonkAction.TWIN_SNAKES, MonkAction.SNAP_PUNCH },
                new MonkAction[] { MonkAction.ARM_OF_THE_DESTROYER, MonkAction.ONE_ILM_PUNCH, MonkAction.ROCKBREAKER },
                new MonkAction[] { MonkAction.FORM_SHIFT, MonkAction.SECOND_WIND, MonkAction.MANTRA }
        };

        private static final MonkAction[] hotbar2 = new MonkAction[] {
                MonkAction.MEDITATION,
                MonkAction.TORNADO_KICK,
                MonkAction.RIDDLE_OF_EARTH,
                null
        };

        private static final MonkAction[][] hotbar3 = new MonkAction[][] {
                new MonkAction[] { MonkAction.RIDDLE_OF_FIRE, MonkAction.INTERNAL_RELEASE, MonkAction.BROTHERHOOD },
                new MonkAction[] { MonkAction.STEEL_PEAK, MonkAction.SHOULDER_TACKLE, MonkAction.HOWLING_FIST },
                new MonkAction[] { null, MonkAction.ELIXIR_FIELD, MonkAction.PERFECT_BALANCE },
                new MonkAction[] { MonkAction.FISTS_OF_WIND, MonkAction.FISTS_OF_FIRE, MonkAction.FISTS_OF_EARTH }
        };

        private static final MonkAction[][] hotbar4 = new MonkAction[][] {
                new MonkAction[] { null, null, null },
                new MonkAction[] { MonkAction.SPRINT, null, null },
                new MonkAction[] { null, null, null },
                new MonkAction[] { null, null, null }
        };

        public static boolean shouldRenderHotbar(InGameHud hud) {
            PlayerEntity cameraPlayer = ((InGameHudAccessor) hud).invokeGetCameraPlayer();
            return cameraPlayer != null && !cameraPlayer.isSpectator() && cameraPlayer.getOffHandStack().isOf(MonkSoul.INSTANCE);
        }

        public static boolean renderHotbar(InGameHud hud, DrawContext ctx, RenderTickCounter tickCounter) {
            PlayerEntity cameraPlayer = ((InGameHudAccessor) hud).invokeGetCameraPlayer();
            if (shouldRenderHotbar(hud)) {
                // 3 + 1 + 3 + 3 with 2px of padding between them =
                // 62 + 2 + 22 + 2 + 62 + 2 + 62
                // = 214
                // so half is 107

                int center = ctx.getScaledWindowWidth() / 2;
                int bottom = ctx.getScaledWindowHeight();

                RenderSystem.enableBlend();
                ctx.getMatrices().push();
                ctx.getMatrices().translate(0.0F, 0.0F, -90.0F);

                ctx.drawGuiTexture(HOTBAR_3X4_TEXTURE, center - 107, bottom - 82, 62, 82);
                ctx.drawGuiTexture(HOTBAR_1X4_TEXTURE, center - 43, bottom - 82, 22, 82);
                ctx.drawGuiTexture(HOTBAR_3X4_TEXTURE, center - 19, bottom - 82, 62, 82);
                ctx.drawGuiTexture(HOTBAR_3X4_TEXTURE, center + 45, bottom - 82, 62, 82);

                ctx.drawGuiTexture(GL_GAUGE, center + 130, bottom - 41, 44, 16);
                ctx.drawGuiTexture(CHAKRA_GAUGE, center + 114, bottom - 25, 60, 15);

                int gl = StatusHelpers.getGreasedLightning(cameraPlayer);
                int chakra = StatusHelpers.getChakra(cameraPlayer);
                if(gl > 0) {
                    long expiration = cameraPlayer.getAttached(PlayerMonkAttachments.greasedLightningExpires());
                    long t = cameraPlayer.getWorld().getTime();
                    Duration timerRemaining = Duration.ofMillis(50 * (expiration - t));
                    long timerText = timerRemaining.toSeconds();
                    // round up
                    if(timerRemaining.toMillisPart() > 0) timerText += 1;

                    ctx.drawTooltip(hud.getTextRenderer(), Text.literal(Long.toString(timerText)),
                            center + 102, bottom - 34 + hud.getTextRenderer().fontHeight);
                }

                ctx.getMatrices().pop();
                RenderSystem.disableBlend();

                RenderSystem.enableBlend();
                ctx.getMatrices().push();

                for(int i = 0; i < gl; i++) {
                    ctx.drawGuiTexture(GL_STACK, center + 132 + (14 * i), bottom - 39, 12, 12);
                }
                for(int i = 0; i < chakra; i++) {
                    ctx.drawGuiTexture(CHAKRA_STACK, center + 116 + (11 * i), bottom - 23, 14, 11);
                }

                ctx.getMatrices().pop();
                RenderSystem.disableBlend();

                for (int row = 0; row < 4; row++) {
                    int y = (3 + (bottom - 82)) + (20 * row);

                    for (int col = 0; col < 3; col++) {
                        int x = (3 + (center - 107)) + (20 * col);

                        MonkAction hotbar1Action = hotbar1[row][col];

                        renderAction(hud, cameraPlayer, ctx, tickCounter, hotbar1Action, x, y);
                    }

                    int hb2x = (3 + (center - 43));
                    MonkAction hotbar2Action = hotbar2[row];
                    renderAction(hud, cameraPlayer, ctx, tickCounter, hotbar2Action, hb2x, y);

                    for (int col = 0; col < 3; col++) {
                        int x = (3 + (center - 19)) + (20 * col);

                        MonkAction hotbar3Action = hotbar3[row][col];

                        renderAction(hud, cameraPlayer, ctx, tickCounter, hotbar3Action, x, y);
                    }

                    for (int col = 0; col < 3; col++) {
                        int x = (3 + (center + 45)) + (20 * col);

                        MonkAction hotbar4Action = hotbar4[row][col];

                        renderAction(hud, cameraPlayer, ctx, tickCounter, hotbar4Action, x, y);
                    }
                }
                return true;
            }
            return false;
        }

        public static void renderAction(InGameHud hud, PlayerEntity cameraPlayer, DrawContext ctx, RenderTickCounter tickCounter, MonkAction action, int x, int y) {
            if(action == null) return;
            action = action.getFullReplacement(cameraPlayer);

            IPlayerEntityExtension playerExt = (IPlayerEntityExtension) cameraPlayer;

            long t = cameraPlayer.getWorld().getTime();
            float d = tickCounter.getTickDelta(true);

            ctx.getMatrices().push();

            ctx.drawGuiTexture(action.getActionIcon(), x, y, 16, 16);

            if(!action.isEnabled(cameraPlayer)) {
                ctx.fill(RenderLayer.getGuiOverlay(), x, y, x + 16, y + 16, 0x7F000000);
            }
            if(action.isHighlighted(cameraPlayer)) {
                ctx.drawGuiTexture(HIGHLIGHT_TEXTURES[(int) (t % HIGHLIGHT_TEXTURES.length)], x - 2, y - 2, 20, 20);
            }

            long targetTickOffCd = playerExt.alymod$getTickOffCooldown(action.getCooldownGroup());
            float targetDeltaOffCd = playerExt.alymod$getDeltaOffCooldown(action.getCooldownGroup());
            boolean inThePast = (targetTickOffCd < t) ||
                    (targetTickOffCd == t &&
                            targetDeltaOffCd < d);
            if(!inThePast) {
                Duration totalDuration = playerExt.alymod$getCooldownDuration(action.getCooldownGroup());

                long remainingTicks = (targetTickOffCd - t);
                float remainingDelta = (targetDeltaOffCd - d);

                Duration remainingDuration = Duration.ofMillis((remainingTicks * 50) + (long) (remainingDelta * 50));

                long nanosTotal = totalDuration.toNanos();
                long nanosRemaining = remainingDuration.toNanos();
                double fractionRemaining = (double) nanosRemaining / (double) nanosTotal;

                int top = y + MathHelper.floor(16.0d * (1.0d - fractionRemaining));
                int height = top + MathHelper.ceil(16.0d * fractionRemaining);

                ctx.fill(RenderLayer.getGuiOverlay(), x, top, x + 16, height, 0x7FFFFFFF);
            }

            ctx.getMatrices().pop();
        }

        private static final Set<Integer> keysymIntercepts = Set.of(
                GLFW.GLFW_KEY_1,
                GLFW.GLFW_KEY_2,
                GLFW.GLFW_KEY_3,
                GLFW.GLFW_KEY_4,
                GLFW.GLFW_KEY_5,
                GLFW.GLFW_KEY_6,
                GLFW.GLFW_KEY_7,
                GLFW.GLFW_KEY_8,
                GLFW.GLFW_KEY_9,
                GLFW.GLFW_KEY_0,
                GLFW.GLFW_KEY_MINUS,
                GLFW.GLFW_KEY_EQUAL,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                GLFW.GLFW_KEY_LEFT_SHIFT
        );
        private static final Set<Integer> mouseIntercepts = Set.of(
                GLFW.GLFW_MOUSE_BUTTON_4,
                GLFW.GLFW_MOUSE_BUTTON_5
        );

        public static boolean interceptActionKeySet(InputUtil.Key key, boolean pressed) {
            if(MinecraftClient.getInstance().currentScreen != null) return false;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player != null && !player.isSpectator() && player.getOffHandStack().isOf(MonkSoul.INSTANCE)) {
                boolean intercept = false;
                if(key.getCategory() == InputUtil.Type.KEYSYM) {
                    intercept = keysymIntercepts.contains(key.getCode());
                } else if(key.getCategory() == InputUtil.Type.MOUSE) {
                    intercept = mouseIntercepts.contains(key.getCode());
                }

                handleKeySet(key, pressed);

                return intercept;
            } else {
                return false;
            }
        }

        public static boolean interceptActionKeyPressed(InputUtil.Key key) {
            if(MinecraftClient.getInstance().currentScreen != null) return false;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player != null && !player.isSpectator() && player.getOffHandStack().isOf(MonkSoul.INSTANCE)) {
                boolean intercept = false;
                if(key.getCategory() == InputUtil.Type.KEYSYM) {
                    intercept = keysymIntercepts.contains(key.getCode());
                } else if(key.getCategory() == InputUtil.Type.MOUSE) {
                    intercept = mouseIntercepts.contains(key.getCode());
                }

                handleKeyPressed(key);

                return intercept;
            } else {
                return false;
            }
        }

        private static boolean controlHeld = false;
        private static boolean shiftHeld = false;

        public static void handleKeySet(InputUtil.Key key, boolean pressed) {
            switch(key.getCode()) {
                case GLFW.GLFW_KEY_LEFT_CONTROL -> controlHeld = pressed;
                case GLFW.GLFW_KEY_LEFT_SHIFT -> shiftHeld = pressed;
            }
        }

        public static void handleKeyPressed(InputUtil.Key key) {
            MonkAction[][] targetHotbar = hotbar1;
            if(controlHeld) targetHotbar = hotbar3;
            else if(shiftHeld) { targetHotbar = hotbar4; }

            int mouseOffset = 0;
            if(shiftHeld) mouseOffset += 2;

            MonkAction act = switch (key.getCode()) {
                case GLFW.GLFW_KEY_1 -> targetHotbar[0][0];
                case GLFW.GLFW_KEY_2 -> targetHotbar[0][1];
                case GLFW.GLFW_KEY_3 -> targetHotbar[0][2];
                case GLFW.GLFW_KEY_4 -> targetHotbar[1][0];
                case GLFW.GLFW_KEY_5 -> targetHotbar[1][1];
                case GLFW.GLFW_KEY_6 -> targetHotbar[1][2];
                case GLFW.GLFW_KEY_7 -> targetHotbar[2][0];
                case GLFW.GLFW_KEY_8 -> targetHotbar[2][1];
                case GLFW.GLFW_KEY_9 -> targetHotbar[2][2];
                case GLFW.GLFW_KEY_0 -> targetHotbar[3][0];
                case GLFW.GLFW_KEY_MINUS -> targetHotbar[3][1];
                case GLFW.GLFW_KEY_EQUAL -> targetHotbar[3][2];
                case GLFW.GLFW_MOUSE_BUTTON_4 -> hotbar2[0 + mouseOffset];
                case GLFW.GLFW_MOUSE_BUTTON_5 -> hotbar2[1 + mouseOffset];
                default -> null;
            };

            if(act != null) {
                doCast(MinecraftClient.getInstance(), act, MinecraftClient.getInstance().player, true);
            }
        }

        public static void doCast(MinecraftClient mc, MonkAction action, ClientPlayerEntity player, boolean queueInput) {
            if(action == null) return;
            action = action.getFullReplacement(player);

            IPlayerEntityExtension playerExt = (IPlayerEntityExtension) player;

            long t = mc.world.getTime();
            float d = mc.getRenderTickCounter().getTickDelta(true);

            long targetTickOffCd = playerExt.alymod$getTickOffCooldown(action.getCooldownGroup());
            float targetDeltaOffCd = playerExt.alymod$getDeltaOffCooldown(action.getCooldownGroup());
            boolean inThePast = (targetTickOffCd < t) ||
                    (targetTickOffCd == t &&
                            targetDeltaOffCd < d);
            if(inThePast) {
                if(!action.isEnabled(player)) return;
                float totalDifference = (t - targetTickOffCd) + (d - targetDeltaOffCd);

                Duration recast = action.getRecast(player);
                if(!action.tryExecuteClient(player, d)) return;

                long calcTickFrom;
                float calcDeltaFrom;
                if(totalDifference > 1.0f) {
                    calcTickFrom = t;
                    calcDeltaFrom = d;
                } else {
                    calcTickFrom = targetTickOffCd;
                    calcDeltaFrom = targetDeltaOffCd;
                }

                long totalMillis = recast.toMillis();
                long totalTicks = totalMillis / 50;
                float totalDelta = (totalMillis % 50) / 50.0F;

                long targetTick = calcTickFrom + totalTicks;
                float targetDelta = calcDeltaFrom + totalDelta;
                if(targetDelta >= 1.0F) {
                    targetTick += 1;
                    targetDelta -= 1.0F;
                }

                playerExt.alymod$setOffCooldown(action.getCooldownGroup(), recast, targetTick, targetDelta);
            } else {
                if(queueInput) playerExt.alymod$setQueuedAction(action, System.nanoTime());
            }
        }

        private static final Duration QUEUE_TIME = Duration.ofMillis(400);

        public static void registerClient() {
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                if(client.player == null) return;

                IPlayerEntityExtension playerExt = (IPlayerEntityExtension) client.player;

                MonkAction queuedAction = playerExt.alymod$getQueuedAction();
                long whenQueue = playerExt.alymod$getQueuedActionNanos();
                long now = System.nanoTime();
                if(queuedAction != null && Duration.ofNanos(now - whenQueue).compareTo(QUEUE_TIME) < 0) {
                    doCast(client, queuedAction, client.player, false);
                }
            });
        }
    }

    public static void register() {
        Registry.register(Registries.ITEM, MonkSoul.ID, MonkSoul.INSTANCE);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        Client.registerClient();
    }

}
