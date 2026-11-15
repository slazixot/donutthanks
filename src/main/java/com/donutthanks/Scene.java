package com.donutthanks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import static com.donutthanks.Fx.*;

/** One running animation. All visuals are computed statelessly from time t (seconds). */
final class Scene {
    private final Tier tier;
    private final String payer;
    private final long amount;

    private DrawContext g;
    private TextRenderer tr;
    private Identifier skin;
    private int w, h;
    private float t;
    private int lastBeat = -1;

    Scene(Tier tier, String payer, long amount) {
        this.tier = tier;
        this.payer = payer;
        this.amount = amount;
    }

    float duration() {
        return tier.duration;
    }

    // ------------------------------------------------------------------ sound

    private void snd(SoundEvent e, float pitch, float vol) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(e, pitch, vol));
    }

    void start() {
        switch (tier) {
            case WAVE, CONFETTI, ORBIT -> snd(SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            case WHALE -> snd(SoundEvents.ENTITY_GENERIC_SPLASH, 0.7f, 1f);
            case FIREWORKS, GOLD -> snd(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            case ROCKET -> snd(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1f);
            case TUNNEL -> snd(SoundEvents.ITEM_TOTEM_USE, 1f, 1f);
            case GOD -> {
                snd(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1f);
                snd(SoundEvents.ITEM_TOTEM_USE, 0.7f, 1f);
            }
        }
    }

    private void beats() {
        int b = (int) (t / tier.beat);
        if (b == lastBeat) return;
        lastBeat = b;
        switch (tier) {
            case WAVE -> snd(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f + (b % 8) * 0.12f, 0.7f);
            case CONFETTI -> snd(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f + rnd(b) * 0.8f, 0.5f);
            case ORBIT -> snd(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f + (b % 6) * 0.2f, 0.7f);
            case WHALE -> snd(SoundEvents.ENTITY_GENERIC_SPLASH, 0.8f + rnd(b) * 0.4f, 0.6f);
            case FIREWORKS -> snd(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f + rnd(b) * 0.5f, 0.8f);
            case GOLD -> snd(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2f + rnd(b) * 0.8f, 0.5f);
            case ROCKET -> {
                if (t < 4.5f) snd(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.7f + t * 0.1f, 0.4f);
                else snd(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f + rnd(b) * 0.5f, 0.9f);
            }
            case TUNNEL -> snd(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f + (b % 12) * 0.12f, 0.8f);
            case GOD -> {
                if (b % 2 == 0) snd(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f + rnd(b) * 0.4f, 0.8f);
                else snd(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f + rnd(b) * 0.5f, 0.8f);
            }
        }
    }

    // ------------------------------------------------------------- primitives

    private void rect(float x, float y, float ww, float hh, int color) {
        int x1 = (int) x, y1 = (int) y;
        int x2 = Math.max(x1 + 1, (int) (x + ww)), y2 = Math.max(y1 + 1, (int) (y + hh));
        g.fill(x1, y1, x2, y2, color);
    }

    private void backdrop(int rgb, float alpha) {
        g.fill(0, 0, w, h, argb((int) alpha, rgb));
    }

    private void part(int x, int y, int u, int v, int pw, int ph) {
        g.drawTexture(RenderLayer::getGuiTextured, skin, x, y, u, v, pw, ph, pw, ph, 64, 64);
    }

    /** Full front view of the player's skin (32 skin-pixels tall), centred on (x, y). */
    private void body(float x, float y, float sx, float sy, float rot) {
        MatrixStack m = g.getMatrices();
        m.push();
        m.translate(x, y, 0f);
        if (rot != 0f) m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rot));
        m.scale(sx, sy, 1f);
        part(-4, -16, 8, 8, 8, 8);     // head
        part(-4, -8, 20, 20, 8, 12);   // torso
        part(-8, -8, 44, 20, 4, 12);   // right arm
        part(4, -8, 36, 52, 4, 12);    // left arm
        part(-4, 4, 4, 20, 4, 12);     // right leg
        part(0, 4, 20, 52, 4, 12);     // left leg
        part(-4, -16, 40, 8, 8, 8);    // hat layer
        m.pop();
    }

    private void body(float x, float y, float s, float rot) {
        body(x, y, s, s, rot);
    }

    private void ring(float cx, float cy, float size, int th, float rot, int color) {
        int s = (int) (size / 2);
        if (s < th * 2) return;
        MatrixStack m = g.getMatrices();
        m.push();
        m.translate(cx, cy, 0f);
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rot));
        g.fill(-s, -s, s, -s + th, color);
        g.fill(-s, s - th, s, s, color);
        g.fill(-s, -s + th, -s + th, s - th, color);
        g.fill(s - th, -s + th, s, s - th, color);
        m.pop();
    }

    private void sparkles(int count, int salt, int rgb) {
        for (int i = 0; i < count; i++) {
            float x = rnd(i * 13 + salt) * w, y = rnd(i * 13 + salt + 1) * h;
            float p = (float) Math.sin(t * 4 + rnd(i * 13 + salt + 2) * TAU);
            if (p < 0.2f) continue;
            int s = 1 + (int) (p * 4);
            int c = argb((int) (p * 255), rgb);
            rect(x - s, y, 2 * s + 1, 1, c);
            rect(x, y - s, 1, 2 * s + 1, c);
        }
    }

    /** Stateless firework bursts with the player skin popping out of each. */
    private void fireworks(float time, float interval, int salt, boolean skins) {
        if (time < 0) return;
        int n = (int) (time / interval);
        for (int b = Math.max(0, n - 4); b <= n; b++) {
            float dt = time - b * interval;
            float life = 2f;
            if (dt < 0 || dt > life) continue;
            float cx = w * (0.15f + 0.7f * rnd(b * 31 + 1 + salt));
            float cy = h * (0.2f + 0.35f * rnd(b * 31 + 2 + salt));
            float hu = rnd(b * 31 + 3 + salt);
            int alpha = (int) (255 * (1f - dt / life));
            for (int p = 0; p < 30; p++) {
                float ang = p * TAU / 30f;
                float speed = 90f + 70f * rnd(b * 131 + p + salt);
                float dist = speed * (1f - (float) Math.exp(-dt * 2.4f));
                float x = cx + (float) Math.cos(ang) * dist;
                float y = cy + (float) Math.sin(ang) * dist + 55f * dt * dt;
                int col = argb(alpha, hue(hu + p * 0.01f));
                rect(x, y, 3, 3, col);
                // short trail
                float d2 = speed * (1f - (float) Math.exp(-(dt - 0.06f) * 2.4f));
                rect(cx + (float) Math.cos(ang) * d2, cy + (float) Math.sin(ang) * d2 + 55f * dt * dt, 2, 2,
                        argb(alpha / 2, hue(hu)));
            }
            if (skins && dt < 1.4f) {
                body(cx, cy - dt * 25f + dt * dt * 40f, 1.5f * (1f - dt / 2f), dt * 120f);
            }
        }
    }

    private void bolt(float x0, int seed, int alpha) {
        float x = x0;
        for (int y = 0; y < h; y += 10) {
            x += (rnd(seed * 97 + y) - 0.5f) * 28f;
            g.fill((int) x - 4, y, (int) x + 5, y + 10, argb(alpha / 4, 0xAAAAFF));
            g.fill((int) x - 1, y, (int) x + 2, y + 10, argb(alpha, 0xFFFFFF));
        }
    }

    private void coin(float x, float y, float size, float spin) {
        float cw = Math.max(1f, Math.abs((float) Math.cos(spin)) * size);
        rect(x - cw / 2 - 1, y - 1, cw + 2, size + 2, argb(255, 0x8A6200));
        rect(x - cw / 2, y, cw, size, argb(255, 0xFFC800));
        if (cw > 4) rect(x - cw / 2 + 2, y + 2, cw - 4, size - 4, argb(255, 0xFFF08A));
    }

    // ------------------------------------------------------------------- text

    private void text(String s, float cx, float y, float scale, int rgb, int alpha, boolean rainbow, float wave) {
        if (alpha < 8) return;
        MatrixStack m = g.getMatrices();
        m.push();
        m.translate(cx, y, 200f);
        m.scale(scale, scale, 1f);
        float x = -tr.getWidth(s) / 2f;
        for (int i = 0; i < s.length(); i++) {
            String c = s.substring(i, i + 1);
            float dy = wave > 0 ? (float) Math.sin(t * 6 + i * 0.5f) * wave : 0f;
            int col = argb(alpha, rainbow ? hue(t * 0.5f + i * 0.06f) : rgb);
            g.drawTextWithShadow(tr, c, Math.round(x), Math.round(dy), col);
            x += tr.getWidth(c);
        }
        m.pop();
    }

    private void title(float fade) {
        float tt = t - tier.titleDelay;
        if (tt < 0) return;
        float pop = easeOutBack(clamp(tt / 0.6f, 0f, 1f));
        float tfade = tier.titleDelay > 0 ? clamp(tt / 0.4f, 0f, 1f) * clamp((tier.duration - t) / 0.8f, 0f, 1f) : fade;
        int a = (int) (tfade * 255);
        float base = tier.ordinal() >= Tier.GOLD.ordinal() ? 4.2f : 3.4f;
        float sc = Math.min(base * pop, w * 0.92f / Math.max(1, tr.getWidth(tier.title)));
        boolean fancy = tier.ordinal() >= Tier.GOLD.ordinal() && tier != Tier.GOLD;
        float y = h * 0.14f;
        text(tier.title, w / 2f, y, sc, tier.color, a, fancy, fancy ? 4f : 0f);
        String sub = payer + " paid you $" + Fx.format(amount);
        float sc2 = Math.min(1.7f * pop, w * 0.8f / Math.max(1, tr.getWidth(sub)));
        text(sub, w / 2f, y + 10f * sc + 6f, sc2, 0xFFFFFF, a, false, 0f);
    }

    // ------------------------------------------------------------------ scenes

    void render(DrawContext ctx, float time) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        g = ctx;
        t = time;
        tr = mc.textRenderer;
        w = ctx.getScaledWindowWidth();
        h = ctx.getScaledWindowHeight();
        skin = mc.player.getSkinTextures().texture();
        float fade = clamp(Math.min(t / 0.5f, (tier.duration - t) / 0.8f), 0f, 1f);

        beats();

        MatrixStack m = g.getMatrices();
        m.push();
        float shake = switch (tier) {
            case WHALE -> 1.5f;
            case GOLD -> 1.5f * Math.max(0f, (float) Math.sin(t * 3f));
            case ROCKET -> t < 4.5f ? 1f + t * 0.8f : 1.5f;
            case TUNNEL -> 2.5f;
            case GOD -> 3.5f;
            default -> 0f;
        } * fade;
        if (shake > 0f) {
            m.translate((rnd((int) (t * 60)) - 0.5f) * 2 * shake, (rnd((int) (t * 60) + 999) - 0.5f) * 2 * shake, 0f);
        }

        switch (tier) {
            case WAVE -> wave(fade);
            case CONFETTI -> confetti(fade);
            case ORBIT -> orbit(fade);
            case WHALE -> whale(fade);
            case FIREWORKS -> fireworksScene(fade);
            case GOLD -> gold(fade);
            case ROCKET -> rocket(fade);
            case TUNNEL -> tunnel(fade);
            case GOD -> god(fade);
        }
        title(fade);
        m.pop();
    }

    /** 5M: a sine wave of your skin sweeping across the bottom of the screen. */
    private void wave(float fade) {
        backdrop(0x0B1A33, 90 * fade);
        // back row
        for (int i = 0; i < w / 26 + 4; i++) {
            float x = ((i * 26f + t * 90f) % (w + 100)) - 50;
            float ph = x * 0.025f - t * 3f + 1.5f;
            body(x, h * 0.66f + (float) Math.sin(ph) * 30f, 0.9f, (float) Math.cos(ph) * 15f);
        }
        // front row
        for (int i = 0; i < w / 22 + 4; i++) {
            float x = ((i * 22f + t * 150f) % (w + 100)) - 50;
            float ph = x * 0.025f - t * 4f;
            body(x, h * 0.78f + (float) Math.sin(ph) * 38f, 1.4f, (float) Math.cos(ph) * 18f);
        }
        for (int x = 0; x < w; x += 3) {
            float yy = h * 0.78f + (float) Math.sin(x * 0.025f - t * 4f) * 38f + 26f;
            g.fill(x, (int) yy, x + 3, h, argb((int) (110 * fade), 0x2288DD));
        }
        sparkles(25, 5, 0x88DDFF);
    }

    /** 10M: skins and confetti raining down. */
    private void confetti(float fade) {
        backdrop(0x1A0B2E, 75 * fade);
        for (int i = 0; i < 150; i++) {
            float sp = 60 + rnd(i * 3 + 2) * 130;
            float y = ((t * sp + rnd(i * 3 + 3) * (h + 60)) % (h + 60)) - 30;
            float x = rnd(i * 3 + 1) * w + (float) Math.sin(t * 2 + i) * 14f;
            rect(x, y, 4, 6, argb(255, hue(rnd(i * 5))));
        }
        for (int i = 0; i < 18; i++) {
            float sp = 45 + rnd(i * 11 + 6) * 60;
            float y = ((t * sp + rnd(i * 11 + 7) * (h + 90)) % (h + 90)) - 45;
            float x = rnd(i * 11 + 5) * w;
            float dir = rnd(i * 11 + 8) > 0.5f ? 1 : -1;
            body(x, y, 1.1f, t * 90f * dir);
        }
    }

    /** 20M: big skin with two rings of skins orbiting it. */
    private void orbit(float fade) {
        backdrop(0x0B2E14, 80 * fade);
        float cx = w / 2f, cy = h * 0.58f;
        for (int i = 0; i < 12; i++) {
            float a = t * 1.2f + i * TAU / 12f;
            float depth = (float) Math.sin(a);
            if (depth < 0) body(cx + (float) Math.cos(a) * 150f, cy + depth * 60f, 1.2f + depth * 0.3f, 0f);
        }
        body(cx, cy + (float) Math.sin(t * 2f) * 6f, 4f, (float) Math.sin(t * 1.5f) * 8f);
        for (int i = 0; i < 12; i++) {
            float a = t * 1.2f + i * TAU / 12f;
            float depth = (float) Math.sin(a);
            if (depth >= 0) body(cx + (float) Math.cos(a) * 150f, cy + depth * 60f, 1.2f + depth * 0.5f, 0f);
        }
        for (int i = 0; i < 6; i++) {
            float a = -t * 1.9f + i * TAU / 6f;
            body(cx + (float) Math.cos(a) * 80f, cy + (float) Math.sin(a) * 30f, 1f, 0f);
        }
        sparkles(35, 9, 0xAAFFAA);
    }

    private float surface(float x, float base) {
        return base + (float) Math.sin(x * 0.012f + t * 2.2f) * 30f + (float) Math.sin(x * 0.031f - t * 3.1f) * 12f;
    }

    /** 50M: rolling ocean, skins surfing the swell, big one in the middle. */
    private void whale(float fade) {
        backdrop(0x021B3A, 110 * fade);
        float base = h * 0.66f;
        for (int x = 0; x < w; x += 4) g.fill(x, (int) surface(x, base), x + 4, h, argb((int) (170 * fade), 0x1E78D2));
        for (int i = 0; i < 8; i++) {
            float x = w * (i + 0.5f) / 8f + (float) Math.sin(t + i) * 18f;
            float y = surface(x, base);
            float slope = (surface(x + 4, base) - surface(x - 4, base)) / 8f;
            body(x, y - 18f, 1.4f, (float) Math.toDegrees(Math.atan(slope)) * 0.8f);
        }
        float cx = w / 2f;
        float cs = surface(cx, base);
        float slope = (surface(cx + 4, base) - surface(cx - 4, base)) / 8f;
        body(cx, cs - 50f, 3.4f, (float) Math.toDegrees(Math.atan(slope)) * 0.7f);
        for (int x = 0; x < w; x += 4) g.fill(x, (int) surface(x + 70, base + 34f), x + 4, h, argb((int) (170 * fade), 0x0A4A99));
        for (int i = 0; i < 50; i++) {
            float life = (t * 0.9f + rnd(i * 7) * 3f) % 1.4f;
            float x = cx + (rnd(i * 7 + 1) - 0.5f) * 240f + (rnd(i * 7 + 2) - 0.5f) * 60f * life;
            float y = cs - 20f - 90f * life + 110f * life * life;
            rect(x, y, 3, 3, argb(200, 0xBFE8FF));
        }
    }

    /** 100M: fireworks show with a crowd of skins jumping. */
    private void fireworksScene(float fade) {
        backdrop(0x05050F, 150 * fade);
        fireworks(t, 0.55f, 0, true);
        for (int i = 0; i < 9; i++) {
            float x = w * (i + 0.5f) / 9f;
            float jump = Math.abs((float) Math.sin(t * 5f + i * 0.9f)) * 22f;
            body(x, h - 34f - jump, 1.6f, 0f);
        }
    }

    /** 500M: spinning gold skin, glow and coin rain. */
    private void gold(float fade) {
        backdrop(0x2A1E00, 125 * fade);
        float cx = w / 2f, cy = h * 0.58f;
        MatrixStack m = g.getMatrices();
        m.push();
        m.translate(cx, cy, 0f);
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t * 20f));
        for (int r = 7; r >= 1; r--) {
            int s = r * 34;
            g.fill(-s, -s, s, s, argb((int) (22 * fade), 0xFFD700));
        }
        m.pop();
        float spin = (float) Math.cos(t * 2.5f);
        if (Math.abs(spin) < 0.12f) spin = spin < 0 ? -0.12f : 0.12f;
        body(cx, cy, 4.6f * spin, 4.6f, 0f);
        for (int i = 0; i < 110; i++) {
            float sp = 90 + rnd(i * 3 + 2) * 130;
            float y = ((t * sp + rnd(i * 3 + 3) * (h + 40)) % (h + 40)) - 20;
            float x = rnd(i * 3 + 1) * w;
            coin(x, y, 8f, t * 5f + i);
        }
        sparkles(40, 21, 0xFFE680);
    }

    /** 1B: rocket launch, white flash, then a fireworks blow-out. */
    private void rocket(float fade) {
        backdrop(0x02020C, 190 * fade);
        float launch = 4.5f;
        if (t < launch) {
            for (int i = 0; i < 90; i++) {
                float sp = 120 + rnd(i * 3 + 2) * 300;
                float y = ((t * sp * (1f + t * 0.4f) + rnd(i * 3 + 3) * h) % h);
                float len = 3 + sp * 0.04f;
                rect(rnd(i * 3 + 1) * w, y, 1, len, argb(180, 0xFFFFFF));
            }
            float p = t / launch;
            float cx = w / 2f, cy = h * 1.05f - p * h * 0.5f;
            for (int k = 0; k < 16; k++) {
                float fx = cx - 3 + (float) Math.sin(t * 30 + k) * 3f;
                rect(fx, cy + 18 + k * 3.5f, 7 - k * 0.3f, 4, argb(255 - k * 14, k < 6 ? 0xFFFF66 : 0xFF6622));
            }
            body(cx, cy, 2.4f, 0f);
        } else {
            float pt = t - launch;
            float cx = w / 2f, cy = h * 0.58f;
            backdrop(0x3A0A00, 70 * fade);
            fireworks(pt, 0.35f, 7, true);
            body(cx, cy, 4.2f + (float) Math.sin(t * 4f) * 0.3f, (float) Math.sin(t * 2f) * 10f);
            sparkles(30, 33, 0xFFAA88);
            if (pt < 0.35f) backdrop(0xFFFFFF, 255 * (1f - pt / 0.35f));
        }
    }

    /** 10B: rainbow tunnel with skins flying at the camera. */
    private void tunnel(float fade) {
        backdrop(0x000000, 160 * fade);
        float cx = w / 2f, cy = h / 2f;
        float big = Math.max(w, h) * 1.5f;
        for (int k = 0; k < 14; k++) {
            float p = (t * 0.5f + k / 14f) % 1f;
            ring(cx, cy, p * p * big, 2 + (int) (p * 12), t * 10f, argb((int) (230 * p), hue(t * 0.3f + p)));
        }
        for (int i = 0; i < 14; i++) {
            float p = (t * 0.35f + i / 14f) % 1f;
            float ang = i * 2.4f + t * 0.8f;
            float r = p * p * w * 0.55f;
            body(cx + (float) Math.cos(ang) * r, cy + (float) Math.sin(ang) * r * 0.6f, 0.4f + p * 3.4f, p * 90f);
        }
        body(cx, cy + 10f, 3.8f + (float) Math.sin(t * 4f) * 0.3f, (float) Math.sin(t * 2f) * 10f);
        sparkles(40, 45, 0xFFFFFF);
    }

    /** 100B: thunder, aura rings, firework storm and an orbiting army of skins. */
    private void god(float fade) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(t * 5f);
        backdrop(0x120000, (150 + 40 * pulse) * fade);
        float cx = w / 2f, cy = h * 0.58f;
        for (int k = 0; k < 5; k++) {
            float p = (t * 0.6f + k / 5f) % 1f;
            ring(cx, cy, p * Math.max(w, h) * 1.3f, 4, t * 30f, argb((int) ((1f - p) * 200), hue(t * 0.4f + k * 0.2f)));
        }
        fireworks(t, 0.5f, 11, false);
        int n = (int) (t / 0.9f);
        float local = t - n * 0.9f;
        if (local < 0.2f) {
            bolt(w * (0.15f + 0.7f * rnd(n * 5 + 1)), n * 3 + 1, 255);
            bolt(w * (0.15f + 0.7f * rnd(n * 5 + 2)), n * 3 + 2, 200);
        }
        for (int i = 0; i < 16; i++) {
            float a = t * 1.1f + i * TAU / 16f;
            float depth = (float) Math.sin(a);
            body(cx + (float) Math.cos(a) * 190f, cy + depth * 70f, 1.2f + (depth + 1f) * 0.35f, 0f);
        }
        for (int i = 0; i < 8; i++) {
            float a = -t * 1.7f + i * TAU / 8f;
            body(cx + (float) Math.cos(a) * 105f, cy + (float) Math.sin(a) * 40f, 1.2f, a * 30f);
        }
        MatrixStack m = g.getMatrices();
        m.push();
        m.translate(cx, cy - 10f + (float) Math.sin(t * 1.6f) * 14f, 0f);
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t * 25f));
        for (int r = 5; r >= 1; r--) {
            int s = 40 + r * 22;
            g.fill(-s, -s, s, s, argb((int) (20 * fade), hue(t * 0.5f + r * 0.1f)));
        }
        m.pop();
        body(cx, cy - 10f + (float) Math.sin(t * 1.6f) * 14f, 5.6f, 0f);
        sparkles(60, 77, 0xFFFFFF);
        if (local < 0.18f) backdrop(0xFFFFFF, 110 * (1f - local / 0.18f));
    }
}
