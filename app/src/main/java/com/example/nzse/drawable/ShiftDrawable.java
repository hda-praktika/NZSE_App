package com.example.nzse.drawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;

import com.example.nzse.App;
import com.example.nzse.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static com.example.nzse.util.Maths.circleFrom;

public class ShiftDrawable extends Drawable {
    private static final float TRIANGLE_SCALED = (float) Math.sqrt(.75);

    private String mString;
    private boolean mInverted = false;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mTextHeight;
    private float mTextWidth;

    @Deprecated
    public ShiftDrawable() {
        this(App.getContext());
    }

    public ShiftDrawable(Context context) {
        Typeface typeface = context.getResources().getFont(R.font.montserrat_semibold);
        mPaint.setTypeface(typeface);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float centerX = getBounds().exactCenterX();
        float centerY = getBounds().exactCenterY();

        canvas.drawText(mString, centerX - .5f * mTextWidth, centerY, mPaint);

        // Gleichseitiges Dreieck mit runden Ecken
        float triangleHeight = .6f * mTextHeight;
        float triangleWidth = triangleHeight * TRIANGLE_SCALED;
        float triangleCornerRadius = .1f * triangleHeight;

        Path triangle = new Path();
        triangle.arcTo(
                circleFrom(0, 0, triangleCornerRadius),
                180,
                120
        );
        triangle.arcTo(
                circleFrom(triangleWidth, .5f * triangleHeight, triangleCornerRadius),
                300,
                120
        );
        triangle.arcTo(
                circleFrom(0, triangleHeight, triangleCornerRadius),
                60,
                120
        );
        triangle.setFillType(Path.FillType.EVEN_ODD);

        canvas.translate(centerX, centerY + .3f * mTextHeight);

        if(mInverted) {
            canvas.rotate(180, 0, .5f * triangleWidth);
        }

        canvas.drawPath(triangle, mPaint);
        canvas.translate(triangleWidth * -.85f, 0);
        canvas.drawPath(triangle, mPaint);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        mPaint.setTextSize(mTextHeight = (bottom - top) * .8f);
        mTextWidth = mPaint.measureText(mString);

        super.setBounds(left, top, right, bottom);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) throws IOException, XmlPullParserException {
        super.inflate(r, parser, attrs, theme);

        @SuppressLint("RestrictedApi")
        final TypedArray a = TypedArrayUtils.obtainAttributes(r, theme, attrs,  new int[] {
                android.R.attr.text,
                android.R.attr.color,
                android.R.attr.layoutDirection
        });

        mString = a.getString(0);
        mPaint.setColor(a.getColor(1, Color.BLACK));
        mInverted = a.getInt(2, 0) == 1;

        a.recycle();
    }
}
