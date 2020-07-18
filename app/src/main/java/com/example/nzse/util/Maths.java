package com.example.nzse.util;

import android.graphics.RectF;

public class Maths {
    public static <T extends Comparable<T>> T clamp(final T val, final T min, final T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    public static RectF circleFrom(float cx, float cy, float radius) {
        return new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
    }
}
