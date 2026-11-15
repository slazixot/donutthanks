package com.donutthanks;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutThanksClient implements ClientModInitializer {
    public static boolean enabled = true;

    /**
     * Matches the WHOLE message only, e.g. "Steve paid you $5M".
     * A player typing it in public chat would have a "Name » " style prefix, so it won't match.
     */
    private static final Pattern PAY = Pattern.compile(
            "^([A-Za-z0-9_.]{2,20}) paid you \\$([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT]?)[.!]?$");

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled || overlay) return;
            String s = message.getString().replaceAll("§.", "").trim();
            Matcher m = PAY.matcher(s);
            if (!m.matches()) return;
            long amount = Fx.toLong(m.group(2), m.group(3));
            Tier tier = Tier.forAmount(amount);
            if (tier == null) return;
            AnimationManager.enqueue(new Scene(tier, m.group(1), amount));
        });

        HudRenderCallback.EVENT.register(AnimationManager::render);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("donutthanks")
                        .then(ClientCommandManager.literal("toggle").executes(c -> {
                            enabled = !enabled;
                            c.getSource().sendFeedback(Text.literal("Donut Thanks " + (enabled ? "enabled" : "disabled")));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("stop").executes(c -> {
                            AnimationManager.stop();
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("test")
                                .then(ClientCommandManager.argument("amount", StringArgumentType.word())
                                        .executes(c -> {
                                            long amt = Fx.parseAmount(StringArgumentType.getString(c, "amount"));
                                            Tier t = amt < 0 ? null : Tier.forAmount(amt);
                                            if (t == null) {
                                                c.getSource().sendFeedback(Text.literal(
                                                        "Use an amount of 5m or more, e.g. /donutthanks test 50m"));
                                                return 0;
                                            }
                                            AnimationManager.enqueue(new Scene(t, "TestPlayer", amt));
                                            return 1;
                                        })))));
    }
}
