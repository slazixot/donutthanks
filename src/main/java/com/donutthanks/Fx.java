package com.donutthanks;

import java.awt.Color;

final class Fx {
    static final float TAU = (float) (Math.PI * 2);

    private Fx() {}

    /** Stateless pseudo random in [0,1). */
    static float rnd(int n) {
        n = (n << 13) ^ n;
        return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 2147483647f;
    }

    static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : Math.min(v, hi);
    }

    static int argb(int a, int rgb) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    static int hue(float h) {
        return Color.HSBtoRGB(h - (float) Math.floor(h), 0.8f, 1f) & 0xFFFFFF;
    }

    static float easeOutBack(float x) {
        float c1 = 1.70158f, c3 = c1 + 1f;
        float p = x - 1f;
        return 1f + c3 * p * p * p + c1 * p * p;
    }

    static String format(long v) {
        if (v >= 1_000_000_000_000L) return trim(v / 1e12) + "T";
        if (v >= 1_000_000_000L) return trim(v / 1e9) + "B";
        if (v >= 1_000_000L) return trim(v / 1e6) + "M";
        if (v >= 1_000L) return trim(v / 1e3) + "K";
        return Long.toString(v);
    }

    private static String trim(double d) {
        String s = String.format(java.util.Locale.ROOT, "%.2f", d);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    /** Parses "5m", "$2.5B", "1,500,000" etc. Returns -1 on failure. */
    static long parseAmount(String raw) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^\\$?([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT]?)$").matcher(raw.trim());
        if (!m.matches()) return -1;
        return toLong(m.group(1), m.group(2));
    }

    static long toLong(String num, String suffix) {
        double v;
        try {
            v = Double.parseDouble(num.replace(",", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
        double mult = switch (suffix.toLowerCase(java.util.Locale.ROOT)) {
            case "k" -> 1e3;
            case "m" -> 1e6;
            case "b" -> 1e9;
            case "t" -> 1e12;
            default -> 1;
        };
        return Math.round(v * mult);
    }
}
