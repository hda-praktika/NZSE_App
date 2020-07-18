package com.example.nzse.drawable;

import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.example.nzse.util.Maths.circleFrom;
import static com.example.nzse.util.Maths.clamp;

public class PlayDrawable extends Drawable {
    private static final String TAG = PlayDrawable.class.getSimpleName();

    private static final int REF_SIZE = 34;
    private static final float CORNER_RADIUS = 2f;
    private static final float TRIANGLE_SCALED = (float) Math.sqrt(.75);

    private float mProgress;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PlayDrawable() {
        mPaint.setColor(0xFF2e2e2e);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int size = getBounds().width();
        float progress = clamp(mProgress, 0f, 1f);

        // Rotation hin zum Dreieck
        float rotation = 90f * progress;
        canvas.rotate(rotation, .5f * size, .5f * size);

        // Kleine Schönheitskorrektur
        canvas.translate(0, size * progress * (-2f / REF_SIZE));

        if (progress < 1) {
            // Hälfte zeichnen
            drawHalf(canvas, size, progress);

            // Spiegeln
            canvas.scale(-1, 1, .5f * size, .5f * size);

            // Andere Hälfte zeichnen
            drawHalf(canvas, size, progress);
        } else {
            drawTriangle(canvas, size);
        }
    }

    private void drawHalf(@NonNull Canvas canvas, int size, float progress) {
        // Die rechten beiden Ecken immer spitzer werden lassen.
        float animCornerRadius = CORNER_RADIUS * (1f - progress);

        // Rechte Kante weiter nach rechts verschieben.
        float rightEdgeOffset = 2f * progress;

        // Untere Kante weiter nach oben verschieben (damit wir ein gleichseitiges Dreieck erhalten).
        float bottomEdgeOffset = progress * ((TRIANGLE_SCALED - 1) * REF_SIZE);

        // Ecke unten links zum linken Rand verschieben.
        float bottomLeftCornerOffset = -6f * progress;

        // Ecke oben links zum rechten Rand verschieben.
        float topLeftCornerOffset = 9f * progress;

        RectF topLeftCircle = circleFrom(
                size * (6f + CORNER_RADIUS + topLeftCornerOffset) / REF_SIZE,
                size * (0f + CORNER_RADIUS) / REF_SIZE,
                size * CORNER_RADIUS / REF_SIZE
        );
        RectF topRightCircle = circleFrom(
                size * (15f - animCornerRadius + rightEdgeOffset) / REF_SIZE,
                size * (0f + animCornerRadius) / REF_SIZE,
                size * animCornerRadius / REF_SIZE
        );
        RectF bottomRightCircle = circleFrom(
                size * (15f - animCornerRadius + rightEdgeOffset) / REF_SIZE,
                size * (34f - animCornerRadius + bottomEdgeOffset) / REF_SIZE,
                size * animCornerRadius / REF_SIZE
        );
        RectF bottomLeftCircle = circleFrom(
                size * (6f + CORNER_RADIUS + bottomLeftCornerOffset) / REF_SIZE,
                size * (34f - CORNER_RADIUS + bottomEdgeOffset) / REF_SIZE,
                size * CORNER_RADIUS / REF_SIZE
        );

        // Hier müssen noch die animierten Winkel berechnet werden.
        float bottomLeftSweepAngle = 90 + 30 * progress;
        float topLeftStartAngle = 180 + 30 * progress;
        float topLeftSweepAngle = 90 - 30 * progress;

        Path path = new Path();
        path.arcTo(topLeftCircle, topLeftStartAngle, topLeftSweepAngle);
        path.arcTo(topRightCircle, 270, 90);
        path.arcTo(bottomRightCircle, 0, 90);
        path.arcTo(bottomLeftCircle, 90, bottomLeftSweepAngle);
        canvas.drawPath(path, mPaint);
    }

    private void drawTriangle(@NonNull Canvas canvas, int size) {
        // Untere Kante weiter nach oben verschieben (damit wir ein gleichseitiges Dreieck erhalten).
        float bottomEdgeOffset = ((TRIANGLE_SCALED - 1) * REF_SIZE);

        RectF topCircle = circleFrom(
                size * 17f / REF_SIZE,
                size * CORNER_RADIUS / REF_SIZE,
                size * CORNER_RADIUS / REF_SIZE
        );
        RectF rightCircle = circleFrom(
                size * (REF_SIZE - CORNER_RADIUS) / REF_SIZE,
                size * (34f - CORNER_RADIUS + bottomEdgeOffset) / REF_SIZE,
                size * CORNER_RADIUS / REF_SIZE
        );
        RectF leftCircle = circleFrom(
                size * CORNER_RADIUS / REF_SIZE,
                size * (34f - CORNER_RADIUS + bottomEdgeOffset) / REF_SIZE,
                size * CORNER_RADIUS / REF_SIZE
        );

        Path path = new Path();
        path.arcTo(topCircle, 210, 120);
        path.arcTo(rightCircle, 330, 120);
        path.arcTo(leftCircle, 90, 120);
        canvas.drawPath(path, mPaint);
    }

    public void setProgress(float progress) {
        mProgress = progress;
        invalidateSelf();
    }

    public float getProgress() {
        return mProgress;
    }

    public void toPlayButton() {
        animateProgress(1);
    }

    public void toPauseButton() {
        animateProgress(0);
    }

    public void animateProgress(float to) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "progress", mProgress, to);
        anim.setDuration(400);
        anim.setAutoCancel(true);
        anim.start();
    }

    public void setPlaying(boolean playing) {
        if(playing) {
            toPauseButton();
        } else {
            toPlayButton();
        }
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        if(right - left != bottom - top) {
            Log.w(TAG, "width != height");
        }

        super.setBounds(left, top, right, bottom);
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
