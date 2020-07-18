package com.example.nzse.drawable;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;

import com.example.nzse.App;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static com.example.nzse.util.Maths.clamp;

/**
 * GlowDrawable fügt einem bestehenden Drawable einen Glüh-Effekt zu.
 * Dies wird realisiert, indem eine Kopie des bestehenden Drawables unter sich
 * selbst gelegt und diese Kopie mit einem Gausschen Weichzeichner versehen wird.
 * Der Weichzeichner wird mittels Renderscript auf der GPU des Smartphones gerendert,
 * sodass kein großer Overhead entsteht.
 * Als Eigenschaften können der Radius und die Transparenz des Glüh-Effekts angegeben werden.
 */
public class GlowDrawable extends Drawable {
    private static final String TAG = GlowDrawable.class.getSimpleName();

    private static final int ALPHA_MIN = 0;
    private static final int ALPHA_MAX = 255;
    private static final float RADIUS_MIN = 0;
    private static final float RADIUS_MAX = 25; // Renderscript Kernel Limit

    private Drawable mBaseDrawable;
    private float mRadius = 15;
    private int mAlpha = 255;

    // Sonstiges
    private int mMargin;
    private int mExtendedWidth;
    private int mExtendedHeight;

    private RenderScript mRenderScript;

    @Deprecated
    public GlowDrawable() {
        mRenderScript = App.getRenderScript();
    }

    public GlowDrawable(@NonNull Drawable baseDrawable) {
        this();
        setBaseDrawable(baseDrawable);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Zeichne Base-Drawable in baseBitmap
        Bitmap baseBitmap = Bitmap.createBitmap(mExtendedWidth, mExtendedHeight, Bitmap.Config.ARGB_8888);
        mBaseDrawable.draw(new Canvas(baseBitmap));

        // Berechne Blur
        Bitmap glowBitmapIn = baseBitmap.copy(Bitmap.Config.ARGB_8888, false);
        Bitmap glowBitmapOut = applyGaussianBlur(glowBitmapIn);
        glowBitmapIn.recycle();

        // Verschiebe Canvas
        canvas.translate(-mMargin, -mMargin);

        // Zeichne Blur
        Paint paint = new Paint();
        paint.setAlpha(mAlpha);
        paint.setColorFilter(new LightingColorFilter(Color.WHITE, 0x202020));
        canvas.drawBitmap(glowBitmapOut, 0, 0, paint);
        glowBitmapOut.recycle();

        // Zeichne normal darüber
        canvas.drawBitmap(baseBitmap, 0, 0, null);
        baseBitmap.recycle();

         // Verschiebe zurück
        canvas.translate(mMargin, mMargin);
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = clamp(alpha, ALPHA_MIN, ALPHA_MAX);
        if(alpha != mAlpha) {
            Log.w(TAG, "Got out-of-range glow alpha: " + alpha);
        }
        invalidateSelf();
    }

    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mBaseDrawable.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        updateBounds();
    }

    public void setBaseDrawable(Drawable drawable) {
        if(drawable == null) {
            throw new RuntimeException("BaseDrawable must not be null");
        }

        mBaseDrawable = drawable;
        updateBounds();
    }

    public Drawable getBaseDrawable() {
        return mBaseDrawable;
    }

    public void setRadius(float radius) {
        mRadius = clamp(radius, RADIUS_MIN, RADIUS_MAX);
        if(mRadius != radius) {
            Log.w(TAG, "Got out-of-range radius: " + radius);
        }

        updateBounds();
    }

    private void updateBounds() {
        // Der Glüheffekt zeichnet i.d.R. über die Grenzen des Base-Drawables hinaus.
        // Beim Weichzeichner gehen diese Grenzen theoretisch bis ins unendliche, wir entscheiden
        // uns aber aus praktischen Gründen, die Grenze auf den Radius zu limitieren.
        mMargin = (int) Math.ceil(mRadius);

        int width = getBounds().width();
        int height = getBounds().height();
        mExtendedWidth = width + 2 * mMargin;
        mExtendedHeight = width + 2 * mMargin;

        mBaseDrawable.setBounds(mMargin, mMargin, width + mMargin, height + mMargin);
    }

    private Bitmap applyGaussianBlur(Bitmap bitmapIn) {
        Bitmap bitmapOut = Bitmap.createBitmap(bitmapIn.getWidth(), bitmapIn.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Einrichten des Kernels und der Buffer
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        Allocation bufferIn = Allocation.createFromBitmap(mRenderScript, bitmapIn);
        Allocation bufferOut = Allocation.createFromBitmap(mRenderScript, bitmapOut);

        // Berechnung des Blurs
        blur.setRadius(mRadius);
        blur.setInput(bufferIn);
        blur.forEach(bufferOut);

        // Kopie des Ergebnisses
        bufferOut.copyTo(bitmapOut);

        return bitmapOut;
    }

    @SuppressLint("ResourceType")
    @Override
    public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) throws IOException, XmlPullParserException {
        super.inflate(r, parser, attrs, theme);

        @SuppressLint("RestrictedApi")
        final TypedArray a = TypedArrayUtils.obtainAttributes(r, theme, attrs,  new int[] {
                android.R.attr.drawable,
                android.R.attr.alpha
        });

        Drawable drawable = a.getDrawable(0);
        setBaseDrawable(drawable);

        mAlpha = Math.round(a.getFloat(1, 1.0f) * 255);

        a.recycle();
    }
}
