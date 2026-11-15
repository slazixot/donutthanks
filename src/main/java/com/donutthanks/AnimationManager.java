package com.donutthanks;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayDeque;

final class AnimationManager {
    private static Scene current;
    private static long startNs;
    private static final ArrayDeque<Scene> QUEUE = new ArrayDeque<>();

    private AnimationManager() {}

    static void enqueue(Scene s) {
        if (QUEUE.size() < 6) QUEUE.add(s);
    }

    static void stop() {
        QUEUE.clear();
        current = null;
    }

    static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (current == null) {
            current = QUEUE.poll();
            if (current == null) return;
            startNs = System.nanoTime();
            current.start();
        }
        float t = (System.nanoTime() - startNs) / 1e9f;
        if (t >= current.duration()) {
            current = null;
            return;
        }
        current.render(ctx, t);
    }
}
