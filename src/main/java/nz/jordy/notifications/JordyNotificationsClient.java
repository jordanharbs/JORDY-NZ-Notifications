package nz.jordy.notifications;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JordyNotificationsClient implements ClientModInitializer {
    private static final Pattern PAYMENT =
            Pattern.compile("^\\s*(.+?)\\s+paid you\\s+(.+?)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final List<PaymentPopup> POPUPS = new ArrayList<>();
    private static final PaymentStore STORE = new PaymentStore();
    private static KeyMapping leaderboardKey;
    private static boolean leaderboardOpen = false;

    @Override
    public void onInitializeClient() {
        STORE.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("jordy_nz_notifications", "main"));
        leaderboardKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.jordy_nz_notifications.leaderboard",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RIGHT_SHIFT,
                category
        ));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> handleMessage(message.getString()));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                handleMessage(message.getString()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long now = System.currentTimeMillis();
            POPUPS.removeIf(p -> now >= p.expiresAt);
            while (leaderboardKey.consumeClick()) {
                leaderboardOpen = !leaderboardOpen;
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("jordy_nz_notifications", "payment_popups"),
                (graphics, deltaTracker) -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.player == null || client.options.hideGui) return;
                    if (leaderboardOpen) {
                        int centerX = client.getWindow().getGuiScaledWidth() / 2;
                        int top = 28;
                        graphics.fill(centerX - 125, top - 10, centerX + 125, top + 235, 0xE6101820);
                        String title = "JORDY NZ - TOP PAYERS";
                        graphics.drawString(client.font, title,
                                centerX - client.font.width(title) / 2, top, 0xFFFFD45C);
                        int y = top + 22;
                        int rank = 1;
                        for (PaymentStore.Entry entry : STORE.topEntries()) {
                            if (rank > 10) break;
                            String row = rank + ". " + entry.name() + "  " + PaymentStore.format(entry.total());
                            graphics.drawString(client.font, row, centerX - 112, y, 0xFFFFFFFF);
                            y += 18;
                            rank++;
                        }
                        String close = "Right Shift to close";
                        graphics.drawString(client.font, close,
                                centerX - client.font.width(close) / 2, top + 215, 0xFFAAAAAA);
                        return;
                    }

                    int y = 12;
                    long now = System.currentTimeMillis();
                    for (PaymentPopup popup : List.copyOf(POPUPS)) {
                        if (now >= popup.expiresAt) continue;
                        String line = popup.player + " paid you " + popup.amount;
                        int width = client.font.width(line);
                        int x = client.getWindow().getGuiScaledWidth() - width - 28;
                        graphics.fill(x - 7, y - 5, x + width + 7, y + 17, 0xCC101820);
                        graphics.fill(x - 7, y - 5, x - 4, y + 17, 0xFF35D07F);
                        graphics.drawString(client.font, line, x, y, 0xFFFFFFFF);
                        y += 28;
                    }
                }
        );
    }

    private static void handleMessage(String raw) {
        Matcher matcher = PAYMENT.matcher(raw);
        if (!matcher.matches()) return;

        String player = matcher.group(1).trim();
        String amount = matcher.group(2).trim();

        // Avoid treating our own overlay or unrelated chat as a payment.
        if (player.isEmpty() || amount.isEmpty() || player.length() > 32 || amount.length() > 40) return;

        POPUPS.add(new PaymentPopup(player, amount, System.currentTimeMillis() + 2000));
        STORE.addPayment(player, parseAmount(amount));
    }

    private static double parseAmount(String text) {
        String cleaned = text.replace(",", "").replace("$", "").trim().toLowerCase();
        try {
            double multiplier = 1;
            if (cleaned.endsWith("k")) { multiplier = 1_000; cleaned = cleaned.substring(0, cleaned.length()-1); }
            else if (cleaned.endsWith("m")) { multiplier = 1_000_000; cleaned = cleaned.substring(0, cleaned.length()-1); }
            else if (cleaned.endsWith("b")) { multiplier = 1_000_000_000; cleaned = cleaned.substring(0, cleaned.length()-1); }
            return Double.parseDouble(cleaned) * multiplier;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private record PaymentPopup(String player, String amount, long expiresAt) {}

}
