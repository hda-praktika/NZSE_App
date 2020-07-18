package com.example.nzse.util;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

public class Colors {
    public static int modifyAlpha(@ColorInt final int color, @IntRange(from = 0, to = 255) final int alpha) {
        return (color & 0xFFFFFF) | (alpha << 24);
    }

    public static int modifyAlpha(@ColorInt final int color, @FloatRange(from = 0f, to = 1f) final float alpha) {
        return modifyAlpha(color, (int)(alpha * 255));
    }

    public static int interpolateHSV(final float fraction, @ColorInt final int start, @ColorInt final int end) {
        float[] startHSV = new float[3];
        float[] endHSV = new float[3];

        Color.colorToHSV(start, startHSV);
        Color.colorToHSV(end, endHSV);

        return Color.HSVToColor(new float[]{
                startHSV[0] + ((endHSV[0] - startHSV[0]) * fraction),
                startHSV[1] + ((endHSV[1] - startHSV[1]) * fraction),
                startHSV[2] + ((endHSV[2] - startHSV[2]) * fraction),
        });
    }
}
