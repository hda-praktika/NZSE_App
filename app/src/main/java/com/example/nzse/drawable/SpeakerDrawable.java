package com.example.nzse.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nzse.App;
import com.example.nzse.R;

import static com.example.nzse.util.Maths.clamp;

/**
 * Animiertes Lautsprecher Icon für das Lautstärke Widget.
 * Inspiriert von https://dribbble.com/shots/9912663-Volume-control
 */
public class SpeakerDrawable extends Drawable {
    private static final String TAG = SpeakerDrawable.class.getSimpleName();

    private static final float TRANSLATIONX_RELATIVE = 6 / 35f;
    private static final float TRANSLATIONX_MUTE_RELATIVE = -3 / 35f;

    public static final float VOLUME_MUTE = -.2f;
    public static final float VOLUME_MIN = 0f;
    public static final float VOLUME_MAX = 1f;

    private float mVolume = 1.0f;

    private Drawable mDrawableSonic1;
    private Drawable mDrawableSonic2;
    private Drawable mDrawableBase;

    private Paint mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SpeakerDrawable() {
        Context context = App.getContext();

        mDrawableSonic1 = context.getResources().getDrawable(R.drawable.ic_speaker_sonic1, context.getTheme());
        mDrawableSonic2 = context.getResources().getDrawable(R.drawable.ic_speaker_sonic2, context.getTheme());
        mDrawableBase = context.getResources().getDrawable(R.drawable.ic_speaker_base, context.getTheme());

        mPaint1.setColor(0xFFFAFAFA);
        mPaint2.setColor(0xFF2E2E2E);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Bitte nicht mit den Zahlen rumspielen. Nach längerem Rumprobieren hat es irgendwann gepasst :D
        int size = getBounds().width();

        // Zeichnen des Lautsprechers und der Schallwellen
        float sonicStrength = (clamp(mVolume, VOLUME_MIN, VOLUME_MAX) - VOLUME_MIN) / (VOLUME_MAX - VOLUME_MIN);
        float muteStrength = 1 - (clamp(mVolume, VOLUME_MUTE, VOLUME_MIN) - VOLUME_MUTE) / (VOLUME_MIN - VOLUME_MUTE);

        float translationX = size * ((1 - sonicStrength) * TRANSLATIONX_RELATIVE + muteStrength * TRANSLATIONX_MUTE_RELATIVE);
        canvas.translate(translationX, 0);

        if(sonicStrength >= 0.5f) {
            mDrawableSonic2.setAlpha((int) ((2f * sonicStrength - 1f) * 255));
            mDrawableSonic2.draw(canvas);
        }

        mDrawableSonic1.setAlpha((int) (clamp(2f * sonicStrength, 0f, 1f) * 255));
        mDrawableSonic1.draw(canvas);

        mDrawableBase.draw(canvas);

        // Zeichnen der Linie, wenn der Lautsprecher gemuted wird.
        float muteLineLength = muteStrength * size;
        canvas.translate((13.5f / 35) * size, 0.5f * size);
        canvas.rotate(315f);
        canvas.drawRoundRect(
                size / 2f - muteLineLength,
                (-2.5f / 35) * size,
                size / 2f,
                (2.5f / 35) * size,
                (5f / 35) * size,
                (5f / 35) * size,
                mPaint1
        );
        canvas.drawRoundRect(
                size / 2f - muteLineLength,
                (-1.5f / 35) * size,
                size / 2f,
                (1.5f / 35) * size,
                (5f / 35) * size,
                (5f / 35) * size,
                mPaint2
        );
    }

    public void setVolume(float volume) {
        mVolume = volume;
        invalidateSelf();
    }

    public float getVolume() {
        return mVolume;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        if(right - left != bottom - top) {
            Log.w(TAG, "width != height");
        }

        super.setBounds(left, top, right, bottom);
        mDrawableSonic2.setBounds(left, top, right, bottom);
        mDrawableSonic1.setBounds(left, top, right, bottom);
        mDrawableBase.setBounds(left, top, right, bottom);
    }

    @Override
    public void setAlpha(int alpha) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mDrawableSonic2.setColorFilter(colorFilter);
        mDrawableSonic1.setColorFilter(colorFilter);
        mDrawableBase.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
