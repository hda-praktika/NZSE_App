package com.example.nzse.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.nzse.R;
import com.example.nzse.drawable.LiveDrawable;
import com.example.nzse.drawable.PlayDrawable;

import static com.example.nzse.util.Callbacks.forwardOnClick;

public class TimeControl extends ConstraintLayout {
    private ActionButton mActionButtonPlay;
    private ActionButton mActionButtonLive;
    private ActionButton mActionButtonShiftBack;
    private ActionButton mActionButtonShiftForward;

    private LiveDrawable mLiveDrawable;
    private PlayDrawable mPlayDrawable;
    private boolean mLive = true;
    private boolean mPlaying;

    private Listener mListener;

    public TimeControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);

        inflate(context, R.layout.widget_time_control, this);
        mActionButtonPlay = findViewById(R.id.actionButton_play);
        mActionButtonLive = findViewById(R.id.actionButton_live);
        mActionButtonShiftBack = findViewById(R.id.actionButton_shiftBack);
        mActionButtonShiftForward = findViewById(R.id.actionButton_shiftForward);

        mLiveDrawable = (LiveDrawable) mActionButtonLive.getIcon();
        mPlayDrawable = (PlayDrawable) mActionButtonPlay.getIcon();
        mPlayDrawable.setPlaying(mPlaying);

        mActionButtonLive.setOnClickListener(forwardOnClick(this, "onClickLive"));
        mActionButtonPlay.setOnClickListener(forwardOnClick(this, "onClickPlay"));
        mActionButtonShiftBack.setOnClickListener(forwardOnClick(this, "onClickShiftBack"));

        mActionButtonShiftForward.setOnClickListener(forwardOnClick(this, "onClickShiftForward"));
    }

    public void setLive(boolean live) {
        if(mLive == live) return;

        mLive = live;
        ObjectAnimator anim = ObjectAnimator.ofFloat(mLiveDrawable, "progress", mLiveDrawable.getProgress(), mLive ? 1 : 0);
        anim.setAutoCancel(true);
        anim.start();
    }

    public void setPlaying(boolean playing) {
        if(mPlaying == playing) return;

        mPlaying = playing;
        mPlayDrawable.setPlaying(playing);
    }

    public void setShiftBackEnabled(boolean enabled) {
        mActionButtonShiftBack.setEnabled(enabled);
    }

    public void setShiftForwardEnabled(boolean enabled) {
        mActionButtonShiftForward.setEnabled(enabled);
    }

    @SuppressWarnings("unused")
    public void onClickLive(View v) {
        if(mLive) return;

        setLive(!mLive);
        if(mListener != null) {
            mListener.onClickLive();
        }
    }

    @SuppressWarnings("unused")
    public void onClickPlay(View v) {
        setPlaying(!mPlaying);

        if(mListener != null) {
            mListener.onClickPlay();
        }
    }

    @SuppressWarnings("unused")
    public void onClickShiftBack(View v) {
        if(mListener != null) {
            mListener.onClickShiftBack();
        }
    }

    @SuppressWarnings("unused")
    public void onClickShiftForward(View v) {
        if(mListener != null) {
            mListener.onClickShiftForward();
        }
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickLive();
        void onClickPlay();
        void onClickShiftBack();
        void onClickShiftForward();
    }
}
