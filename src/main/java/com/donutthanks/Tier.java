package com.donutthanks;

public enum Tier {
    WAVE(5_000_000L, "5M", "THANK YOU FOR THE GAMBLE!", 0x55FFFF, 8f, 0f, 0.5f),
    CONFETTI(10_000_000L, "10M", "BIG SPENDER!", 0xFF55FF, 8f, 0f, 0.6f),
    ORBIT(20_000_000L, "20M", "ABSOLUTE LEGEND!", 0x55FF55, 9f, 0f, 0.5f),
    WHALE(50_000_000L, "50M", "WHALE ALERT!", 0x3399FF, 10f, 0f, 1.2f),
    FIREWORKS(100_000_000L, "100M", "100 MILLION CLUB!", 0xFFAA00, 11f, 0f, 0.55f),
    GOLD(500_000_000L, "500M", "HALF A BILLION!", 0xFFD700, 12f, 0f, 0.3f),
    ROCKET(1_000_000_000L, "1B", "BILLIONAIRE!", 0xFF5555, 13f, 4.5f, 0.35f),
    TUNNEL(10_000_000_000L, "10B", "MEGA BILLIONAIRE!", 0xAA55FF, 14f, 0f, 0.4f),
    GOD(100_000_000_000L, "100B", "GOD TIER DONATION!", 0xFFFFFF, 16f, 0f, 0.9f);

    public final long min;
    public final String label;
    public final String title;
    public final int color;
    public final float duration;
    public final float titleDelay;
    public final float beat;

    Tier(long min, String label, String title, int color, float duration, float titleDelay, float beat) {
        this.min = min;
        this.label = label;
        this.title = title;
        this.color = color;
        this.duration = duration;
        this.titleDelay = titleDelay;
        this.beat = beat;
    }

    /** Highest tier whose threshold is <= amount, or null if below 5M. */
    public static Tier forAmount(long amount) {
        Tier best = null;
        for (Tier t : values()) if (amount >= t.min) best = t;
        return best;
    }

    public static Tier byLabel(String s) {
        for (Tier t : values()) if (t.label.equalsIgnoreCase(s)) return t;
        return null;
    }
}
