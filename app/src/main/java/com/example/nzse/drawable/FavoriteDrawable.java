package com.example.nzse.drawable;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nzse.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static com.example.nzse.util.Colors.interpolateHSV;
import static com.example.nzse.util.Colors.modifyAlpha;
import static com.example.nzse.util.Maths.clamp;

public class FavoriteDrawable extends Drawable {
    private static final String TAG = FavoriteDrawable.class.getSimpleName();

    private static final int STAR_POINTS = 5;
    private float mProgress;
    private Drawable mStarDrawable;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintTint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ObjectAnimator mObjectAnimator;

    private static final int PALETTE_DISABLED = 0;
    private static final int PALETTE_ENABLED = 1;
    private static final int PALETTE_DOT_OFFSET = 2;
    @ColorInt private int[] mColorPalette;

    @Deprecated
    public FavoriteDrawable() { }

    public FavoriteDrawable(Context context) {
        init(context.getResources(), context.getTheme());
    }

    private void init(Resources r, Resources.Theme theme) {
        mStarDrawable = r.getDrawable(R.drawable.ic_star, theme);
        mColorPalette = r.getIntArray(R.array.starPalette);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int size = getBounds().width();
        float progress = clamp(mProgress, 0f, 1f);

        float dotOffset = size * (.5f - progress);
        float dotRadius = size * (.1f + progress * .1f);
        float dotTransparency = 1 - progress;

        // Halbe Umdrehung drehen
        canvas.rotate(360f / 2 / STAR_POINTS, size / 2f, size / 2f);

        // Punkte malen
        for(int i = 0; i < STAR_POINTS; i++) {
            mPaint.setColor(modifyAlpha(mColorPalette[PALETTE_DOT_OFFSET + i], dotTransparency));

            canvas.drawCircle(.5f * size, dotOffset, dotRadius, mPaint);

            canvas.rotate(360f / STAR_POINTS, .5f * size, .5f * size);
        }

        // Halbe Umdrehung zurück
        canvas.rotate(-360f / 2 / STAR_POINTS, size / 2f, size / 2f);

        float starScale = 1 + .4f * ((float) Math.sin(progress * progress * Math.PI));
        canvas.scale(starScale, starScale, .5f * size, .5f * size);

        // Stern in jeweiliger Farbe drübermalen
        Bitmap starBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas starCanvas = new Canvas(starBitmap);
        mStarDrawable.draw(starCanvas);
        int starColor = interpolateHSV(progress, mColorPalette[PALETTE_DISABLED], mColorPalette[PALETTE_ENABLED]);
        mPaintTint.setColorFilter(new PorterDuffColorFilter(starColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(starBitmap, 0, 0, mPaintTint);
        starBitmap.recycle();
    }

    public void setEnabled(boolean enabled) {
        if(mObjectAnimator != null) mObjectAnimator.cancel();

        if(enabled) {
            mObjectAnimator = ObjectAnimator.ofFloat(this, "progress", mProgress, 1);
            mObjectAnimator.setDuration(500);
            mObjectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mObjectAnimator.start();
        } else {
            setProgress(0f);
        }
    }

    public boolean isEnabled() {
        return mProgress > 0;
    }

    public void setProgress(float progress) {
        mProgress = progress;
        invalidateSelf();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        if(right - left != bottom - top) {
            Log.w(TAG, "width != height");
        }

        super.setBounds(left, top, right, bottom);

        mStarDrawable.setBounds(left, top, right, bottom);
    }

    @Override
    public int getIntrinsicWidth() {
        return mStarDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mStarDrawable.getIntrinsicHeight();
    }

    @Override
    public void setAlpha(int alpha) {
        // throw new RuntimeException("Not implemented");
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // throw new RuntimeException("Not implemented");
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) throws IOException, XmlPullParserException {
        super.inflate(r, parser, attrs, theme);
        init(r, theme);
    }
}
