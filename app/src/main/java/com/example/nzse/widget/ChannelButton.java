package com.example.nzse.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.nzse.R;
import com.example.nzse.drawable.LedDrawable;

public class ChannelButton extends ConstraintLayout {
    public enum LedState {
        ACTIVE,     // Kanal wird gerade abgespielt
        BACKGROUND, // Kanal ist Bild-in-Bild
        HIDDEN      // Keine LED sichtbar
    };

    private ImageView mImageView;
    private ImageView mImageViewLed;
    private LedDrawable mLedDrawable;
    private LedState mLedState;

    @ColorInt private int mColorLedActive;
    @ColorInt private int mColorLedBackground;

    public ChannelButton(Context context) {
        super(context);
        init(null);
    }

    public ChannelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.widget_channel_button, this);
        mImageView = findViewById(R.id.imageView);
        mImageViewLed = findViewById(R.id.imageView_led);

        mLedDrawable = (LedDrawable) mImageViewLed.getDrawable();

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ChannelButton, R.attr.channelButtonStyle, 0);

        // Icon
        Drawable icon = a.getDrawable(R.styleable.ChannelButton_icon);
        mImageView.setImageDrawable(icon);

        // Led Colors
        mColorLedActive = a.getColor(R.styleable.ChannelButton_colorLedActive, 0);
        mColorLedBackground = a.getColor(R.styleable.ChannelButton_colorLedBackground, 0);

        // Led State
        LedState state = LedState.values()[a.getInt(R.styleable.ChannelButton_led_state, LedState.HIDDEN.ordinal())];
        setLedState(state);

        a.recycle();

        setClickable(true);
    }

    public void setLedState(LedState state) {
        mLedState = state;
        switch(state) {
            case ACTIVE:
                mLedDrawable.setColor(mColorLedActive);
                mImageViewLed.setVisibility(View.VISIBLE);
                break;

            case BACKGROUND:
                mLedDrawable.setColor(mColorLedBackground);
                mImageViewLed.setVisibility(View.VISIBLE);
                break;

            case HIDDEN:
                mImageViewLed.setVisibility(View.GONE);
                break;
        }
    }

    public LedState getLedState() {
        return mLedState;
    }

    public void setLedProgress(float progress) {
        mLedDrawable.setProgress(progress);
    }

    public void setIcon(Drawable icon) {
        mImageView.setImageDrawable(icon);
    }
}
