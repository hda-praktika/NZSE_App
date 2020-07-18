package com.example.nzse.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nzse.App;
import com.example.nzse.R;

import static com.example.nzse.util.Colors.interpolateHSV;
import static com.example.nzse.util.Colors.modifyAlpha;

public class LiveDrawable extends Drawable {
    private static final String TEXT = "Live";

    private float mProgress = 1f;
    private int mColorInactive = 0xff555555;
    private int mColorActive = 0xffF61818;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mTextHeight;
    private float mTextWidth;

    @Deprecated
    public LiveDrawable() {
        this(App.getContext());
    }

    public LiveDrawable(Context context) {
        Typeface typeface = context.getResources().getFont(R.font.montserrat_semibold);
        mPaint.setColor(mColorActive);
        mPaint.setTypeface(typeface);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float centerX = getBounds().exactCenterX();
        float centerY = getBounds().exactCenterY();

        canvas.drawCircle(centerX, centerY - mTextHeight * .6f, mTextHeight * .3f, mPaint);
        canvas.drawText("Live", centerX - mTextWidth / 2, centerY + mTextHeight * .7f, mPaint);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        mPaint.setTextSize(mTextHeight = (bottom - top) * .8f);
        mTextWidth = mPaint.measureText(TEXT);
        updatePaint(right - left, bottom - top);

        super.setBounds(left, top, right, bottom);
    }

    private void updatePaint(int width, int height) {
        int interpolatedColor = interpolateHSV(mProgress, mColorInactive, mColorActive);
        mPaint.setColor(interpolatedColor);
        mPaint.setShadowLayer(Math.min(width, height) * .4f, 0, 0, modifyAlpha(interpolatedColor, .8f * mProgress));
    }

    public void setProgress(float progress) {
        mProgress = progress;
        updatePaint(getBounds().width(), getBounds().height());
        invalidateSelf();
    }

    public float getProgress() {
        return mProgress;
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
