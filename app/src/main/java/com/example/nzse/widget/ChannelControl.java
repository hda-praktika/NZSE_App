package com.example.nzse.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.nzse.R;
import com.example.nzse.drawable.FavoriteDrawable;

import static com.example.nzse.util.Callbacks.forwardOnClick;

public class ChannelControl extends ConstraintLayout implements View.OnClickListener {
    public static final int BUTTON_MORE = 5;

    private TextView mTextViewProvider;
    private TextView mTextViewProgram;
    private ActionButton mButtonPreviousChannel;
    private ActionButton mButtonNextChannel;
    private GridLayout mGridLayout;
    private ImageView mImageViewFavorite;
    private ChannelButton[] mChannelButtons = new ChannelButton[6];
    private Listener mListener;

    private FavoriteDrawable mFavoriteDrawable;
    private boolean mFavorite = false;

    public ChannelControl(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.widget_channel_control, this);

        mTextViewProvider = findViewById(R.id.textView_provider);
        mTextViewProgram = findViewById(R.id.textView_program);
        mButtonPreviousChannel = findViewById(R.id.circularButton_previous);
        mButtonNextChannel = findViewById(R.id.circularButton_next);
        mGridLayout = findViewById(R.id.gridLayout);
        mImageViewFavorite = findViewById(R.id.imageView_favorite);
        for(int i = 0, len = mGridLayout.getChildCount(); i < len; i++) {
            mChannelButtons[i] = (ChannelButton) mGridLayout.getChildAt(i);
            mChannelButtons[i].setOnClickListener(this);
        }

        mFavoriteDrawable = (FavoriteDrawable) mImageViewFavorite.getDrawable();
        mImageViewFavorite.setOnClickListener(forwardOnClick(this, "onClickFavorite"));
        mButtonPreviousChannel.setOnClickListener(forwardOnClick(this, "onClickPreviousChannel"));
        mButtonNextChannel.setOnClickListener(forwardOnClick(this, "onClickNextChannel"));

        startLedAnimation();
    }

    @SuppressWarnings("unused")
    public void onClickFavorite(View v) {
        if(mListener != null) {
            mListener.onClickFavorite();
        }
    }

    private void startLedAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "ledProgress", 0, 1);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setDuration(1800);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    @SuppressWarnings("unused")
    public void setLedProgress(float progress) {
        for(ChannelButton button : mChannelButtons) {
            button.setLedProgress(progress);
        }
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        if(mListener == null) return;

        for(int i = 0; i < mChannelButtons.length; i++) {
            if(mChannelButtons[i] == v) {
                mListener.onClickChannel(i);
                return;
            }
        }
    }

    @SuppressWarnings("unused")
    public void onClickPreviousChannel(View v) {
        if(mListener != null) {
            mListener.onClickPreviousChannel();
        }
    }

    @SuppressWarnings("unused")
    public void onClickNextChannel(View v) {
        if(mListener != null) {
            mListener.onClickNextChannel();
        }
    }

    public void setActiveProvider(String provider) {
        mTextViewProvider.setText(provider);
    }

    public void setActiveProgram(String program) {
        mTextViewProgram.setText(program);
    }

    public void setChannelIcon(int index, Drawable icon) {
        mChannelButtons[index].setIcon(icon);
    }

    public void setActiveChannel(int index) {
        for(int i = 0; i < 6; i++) {
            if(mChannelButtons[i].getLedState() == ChannelButton.LedState.ACTIVE) {
                mChannelButtons[i].setLedState(ChannelButton.LedState.HIDDEN);
            }
        }

        if(index >= 0 && index < BUTTON_MORE)
            mChannelButtons[index].setLedState(ChannelButton.LedState.ACTIVE);
    }

    public void setBackgroundChannel(int index) {
        for(int i = 0; i < 6; i++) {
            if(mChannelButtons[i].getLedState() == ChannelButton.LedState.BACKGROUND) {
                mChannelButtons[i].setLedState(ChannelButton.LedState.HIDDEN);
            }
        }

        if(index >= 0 && index < BUTTON_MORE)
            mChannelButtons[index].setLedState(ChannelButton.LedState.BACKGROUND);
    }

    public void setFavorite(boolean favorite, boolean animate) {
        mFavorite = favorite;
        if(animate) {
            mFavoriteDrawable.setEnabled(favorite);
        } else {
            mFavoriteDrawable.setProgress(favorite ? 1f : 0f);
        }
    }

    public interface Listener {
        void onClickFavorite();
        void onClickPreviousChannel();
        void onClickNextChannel();
        void onClickChannel(int index);
    }
}
