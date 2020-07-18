package com.example.nzse.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.nzse.R;
import com.example.nzse.drawable.GlowDrawable;

public class IconSwitch extends ConstraintLayout implements View.OnClickListener {
    private float mState;
    private boolean mToggled;

    private ActionButton mActionButton;

    private IconSwitchBackground mIconSwitchBackground;
    private ActionButtonIcon mActionButtonIcon;

    private OnToggleListener mListener;

    public IconSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.widget_icon_switch, this);

        mActionButton = findViewById(R.id.actionButton);
        mActionButton.setOnClickListener(this);

        applyAttributes(attrs);
    }

    private void applyAttributes(@Nullable AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IconSwitch);

        Drawable start = a.getDrawable(R.styleable.IconSwitch_iconStart);
        Drawable end = a.getDrawable(R.styleable.IconSwitch_iconEnd);
        setBackground(mIconSwitchBackground = new IconSwitchBackground(start, end));

        start = a.getDrawable(R.styleable.IconSwitch_iconStart);
        end = a.getDrawable(R.styleable.IconSwitch_iconEnd);
        mActionButton.setIcon(mActionButtonIcon = new ActionButtonIcon(start, end));

        a.recycle();
    }

    public void setState(float state) {
        mState = state;

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mActionButton.getLayoutParams();
        lp.horizontalBias = state;
        mActionButton.setLayoutParams(lp);
        mActionButtonIcon.setProgress(state);
    }

    public float getState() {
        return mState;
    }

    public void setToggled(boolean toggled) {
        if(mToggled == toggled) return;

        mToggled = toggled;
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "state", getState(), toggled ? 1 : 0);
        anim.setAutoCancel(true);
        anim.start();
    }

    public boolean isToggled() {
        return mToggled;
    }

    public void setOnToggleListener(@Nullable OnToggleListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        setToggled(!mToggled);

        if(mListener != null) {
            mListener.onToggle(mToggled);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mActionButton.onTouchEvent(event);
    }

    private class IconSwitchBackground extends Drawable {
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Drawable mStart;
        private Drawable mEnd;

        private IconSwitchBackground(Drawable start, Drawable end) {
            mPaint.setColor(getContext().getColor(R.color.darkBlue));
            mStart = start.mutate();
            mEnd = end.mutate();

            int light = getContext().getColor(R.color.light);
            mStart.setTint(light);
            mEnd.setTint(light);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            int width = getBounds().width();
            int height = getBounds().height();

            float cornerRadius = .5f * Math.min(width, height);
            canvas.drawRoundRect(
                    getBounds().left,
                    getBounds().top,
                    getBounds().right,
                    getBounds().bottom,
                    cornerRadius,
                    cornerRadius,
                    mPaint
            );

            mStart.draw(canvas);
            mEnd.draw(canvas);
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            mStart.setBounds(calcStartBounds(left, top, right, bottom));
            mEnd.setBounds(calcEndBounds(left, top, right, bottom));

            super.setBounds(left, top, right, bottom);
        }

        private Rect calcStartBounds(int left, int top, int right, int bottom) {
            Rect rect = calcChildBounds(left, top, right, bottom);
            rect.right = rect.left + rect.height();
            return rect;
        }

        private Rect calcEndBounds(int left, int top, int right, int bottom) {
            Rect rect = calcChildBounds(left, top, right, bottom);
            rect.left = rect.right - rect.height();
            return rect;
        }

        private Rect calcChildBounds(int left, int top, int right, int bottom) {
            float iconSize = mActionButton.getIconSize();
            float buttonWidth = mActionButton.getWidth();
            float buttonMargin = ((MarginLayoutParams) mActionButton.getLayoutParams()).leftMargin;

            float childMarginHorizontal = buttonMargin + (buttonWidth - iconSize) / 2;
            float childMarginVertical = (bottom - top - iconSize) / 2;
            return new Rect(
                    (int)(left + childMarginHorizontal),
                    (int)(top + childMarginVertical),
                    (int)(right - childMarginHorizontal),
                    (int)(bottom - childMarginVertical)
            );
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

    private class ActionButtonIcon extends Drawable {
        private float mProgress = 0f;
        private GlowDrawable mStart;
        private GlowDrawable mEnd;

        public ActionButtonIcon(Drawable start, Drawable end) {
            mStart = new GlowDrawable(start);
            mEnd = new GlowDrawable(end);
        }

        private void setProgress(float progress) {
            mProgress = progress;
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            float parentWidth = getMeasuredWidth();
            float iconSize = mActionButton.getIconSize();
            float buttonWidth = mActionButton.getWidth();
            float buttonMargin = ((MarginLayoutParams) mActionButton.getLayoutParams()).leftMargin;
            float maxTranslationX = parentWidth - 2 * buttonMargin - buttonWidth;

            float translationX = mProgress * -maxTranslationX;
            canvas.translate(translationX, 0);

            mStart.draw(canvas);
            canvas.translate(maxTranslationX, 0);
            mEnd.draw(canvas);
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            mStart.setBounds(left, top, right, bottom);
            mEnd.setBounds(left, top, right, bottom);
            super.setBounds(left, top, right, bottom);
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

    public interface OnToggleListener {
        void onToggle(boolean enabled);
    }
}
