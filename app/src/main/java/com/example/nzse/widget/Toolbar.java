package com.example.nzse.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.nzse.R;

public class Toolbar extends ConstraintLayout {
    private boolean mInverted;

    private View mRectangle;
    private ImageView mImageView;
    private ActionButton mActionButton;
    private ConstraintLayout mContentView;

    /**
     * Wird gesetzt, damit zukünftige Aufrufe von addView auf der eigentlichen
     * Content View durchgeführt werden und nicht auf der Root View.
     */
    private boolean mInflateDone;

    public Toolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.widget_toolbar, this);
        mInflateDone = true;

        mRectangle = findViewById(R.id.view_rectangle);
        mContentView = findViewById(R.id.content);
        mImageView = findViewById(R.id.curvature);
        mActionButton = findViewById(R.id.actionButton);

        adjustRectangle();
        applyAttributes(attrs);
    }

    private void adjustRectangle() {
        ViewGroup.LayoutParams lp = mRectangle.getLayoutParams();
        lp.height = getStatusBarHeight();
        mRectangle.setLayoutParams(lp);
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.Toolbar, 0, 0);

        boolean inverted = a.getBoolean(R.styleable.Toolbar_inverted, false);
        setInverted(inverted);

        a.recycle();
    }

    public ActionButton getActionButton() {
        return mActionButton;
    }

    @Override
    public void addView(View child, int index) {
        if(!mInflateDone) {
            super.addView(child, index);
        } else {
            mContentView.addView(child, index);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if(!mInflateDone) {
            super.addView(child, width, height);
        } else {
            mContentView.addView(child, width, height);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if(!mInflateDone) {
            super.addView(child, index, params);
        } else {
            mContentView.addView(child, index, params);
        }
    }

    public void setInverted(boolean inverted) {
        if(mInverted != inverted){
            mInverted = inverted;

            // Inhalt der ImageView muss gespiegelt werden
            mImageView.setScaleX(inverted ? -1 : 1);

            // Constraints von ImageView spiegeln
            {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mImageView.getLayoutParams();
                int tmp = params.startToStart;
                params.startToStart = params.endToEnd;
                params.endToEnd = tmp;
                mImageView.setLayoutParams(params);
            }

            // Constraints und Margins von Button spiegeln
            {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mActionButton.getLayoutParams();

                // Constraints
                int tmp = params.startToStart;
                params.startToStart = params.endToEnd;
                params.endToEnd = tmp;

                // Margins
                tmp = params.getMarginStart();
                params.setMarginStart(params.getMarginEnd());
                params.setMarginEnd(tmp);

                mActionButton.setLayoutParams(params);
            }

            // Margins von der Content View spiegeln
            {
                ConstraintLayout.LayoutParams params = (LayoutParams) mContentView.getLayoutParams();
                int tmp = params.getMarginStart();
                params.setMarginStart(params.getMarginEnd());
                params.setMarginEnd(tmp);
                mContentView.setLayoutParams(params);
            }
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
