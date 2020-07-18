package com.example.nzse.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.example.nzse.util.Colors.modifyAlpha;
import static com.example.nzse.util.Maths.clamp;

public class LedDrawable extends Drawable {
    @ColorInt private int mColor;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mProgress = 0f;

    public void setColor(@ColorInt int color) {
        mColor = color;
    }

    public void setProgress(float progress) {
        mProgress = progress;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = getBounds().width();
        int height = getBounds().height();

        // Transparenten Kreis zeichnen
        float transparency = .8f * (1 - clamp(mProgress, 0f, 1f));
        float offset = .5f * clamp(mProgress, 0f, 1f);
        float offsetWidth = offset * width;
        float offsetHeight = offset * height;

        mPaint.setColor(modifyAlpha(mColor, transparency));
        canvas.drawOval(-offsetWidth, -offsetHeight, width + offsetWidth, height + offsetHeight, mPaint);

        // Gefüllten Kreis zeichnen
        mPaint.setColor(modifyAlpha(mColor, 255));
        canvas.drawOval(0, 0, width, height, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
