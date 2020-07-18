package com.example.nzse.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;

import com.example.nzse.R;

public class ActionButton extends LinearLayout {
    private ImageView mImageView;

    public ActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.widget_action_button, this);
        mImageView = findViewById(R.id.imageView);

        applyAttributes(attrs);
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ActionButton);

        Drawable icon = a.getDrawable(R.styleable.ActionButton_icon);
        setIcon(icon);

        int iconSizeDp = a.getInt(R.styleable.ActionButton_icon_size, 22);
        int iconSizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                iconSizeDp,
                getResources().getDisplayMetrics()
        );
        setIconSize(iconSizePx);

        a.recycle();
    }

    public void setIcon(Drawable icon) {
        mImageView.setImageDrawable(icon);
    }

    public void setIcon(@DrawableRes int iconRes) {
        mImageView.setImageResource(iconRes);
    }

    public Drawable getIcon() {
        return mImageView.getDrawable();
    }

    private void setIconSize(int iconSize) {
        ViewGroup.LayoutParams lp = mImageView.getLayoutParams();
        lp.width = lp.height = iconSize;
        mImageView.setLayoutParams(lp);
    }

    public int getIconSize() {
        return mImageView.getLayoutParams().width;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if(isEnabled() == enabled) return;
        super.setEnabled(enabled);

        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), enabled ? 1.0f : 0.6f);
        anim.setAutoCancel(true);
        anim.start();
    }
}
