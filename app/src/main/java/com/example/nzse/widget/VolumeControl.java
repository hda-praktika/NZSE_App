package com.example.nzse.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.example.nzse.R;
import com.example.nzse.drawable.SpeakerDrawable;

import static com.example.nzse.util.Callbacks.forwardOnTouch;

public class VolumeControl extends LinearLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private static final TimeInterpolator sInterpolator = new AccelerateDecelerateInterpolator();
    private static final int TYPE_BOTH = 0;
    private static final int TYPE_SPEAKER_ONLY = 1;

    private ActionButton mActionButton;
    private AppCompatSeekBar mSeekBar;

    private SpeakerDrawable mSpeakerDrawable;

    private int mRememberedVolume;
    private int mVolume = 100;
    private boolean mMuted = false;
    private Listener mListener = null;

    private boolean mButtonClickable = true;
    private int mActiveAnimationType;
    private ObjectAnimator mObjectAnimator;

    public VolumeControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);

        setOrientation(HORIZONTAL);
        inflate(context, R.layout.widget_volume_control, this);

        mActionButton = findViewById(R.id.actionButton);
        mSeekBar = findViewById(R.id.seekbar);
        mSpeakerDrawable = (SpeakerDrawable) mActionButton.getIcon();

        mActionButton.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setOnTouchListener(forwardOnTouch(this, "onTouchSeekBar"));

        mSpeakerDrawable.setVolume(mMuted ? SpeakerDrawable.VOLUME_MUTE : mVolume / 100f);
        mSeekBar.setProgress(mMuted ? 0 : mVolume);

        mObjectAnimator = new ObjectAnimator();
        mObjectAnimator.setTarget(this);
        mObjectAnimator.setInterpolator(sInterpolator);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(!fromUser) return;

        animateSpeakerIconOnly(progress);

        updateAndNotifyMuted(progress == 0);
        updateAndNotifyVolume(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mButtonClickable = false;
        mRememberedVolume = mVolume;

        // Laufende Animationen beenden
        if(mObjectAnimator != null && mObjectAnimator.isStarted()) {
            mObjectAnimator.cancel();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mButtonClickable = true;
        if(mVolume == 0) {
            updateAndNotifyVolume(mRememberedVolume);
        }
    }

    @SuppressWarnings("unused")
    public boolean onTouchSeekBar(View v, MotionEvent event) {
        // Unterbinden von Touch-Events während Animationen aktiv sind.
        // Siehe: https://stackoverflow.com/a/16236736/6338257
        return (mObjectAnimator != null && mObjectAnimator.isStarted() && mActiveAnimationType == TYPE_BOTH) || mSeekBar.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if(!mButtonClickable) return;

        updateAndNotifyMuted(!mMuted);
        animateSpeakerIconAndSeekBar(mMuted, mVolume);
    }

    private void animateSpeakerIconAndSeekBar(boolean mute, int volume) {
        PropertyValuesHolder speakerVolume;
        PropertyValuesHolder seekBarProgress;

        int distance;
        if(mute) {
            speakerVolume = PropertyValuesHolder.ofFloat("speakerVolume", mSpeakerDrawable.getVolume(), SpeakerDrawable.VOLUME_MUTE);
            seekBarProgress = PropertyValuesHolder.ofInt("seekBarProgress", mSeekBar.getProgress(), 0);

            distance = mSeekBar.getProgress();
        } else {
            speakerVolume = PropertyValuesHolder.ofFloat("speakerVolume", SpeakerDrawable.VOLUME_MUTE, volume / 100f);
            seekBarProgress = PropertyValuesHolder.ofInt("seekBarProgress", mSeekBar.getProgress(), volume);

            distance = Math.abs(mSeekBar.getProgress() - volume);
        }

        mObjectAnimator.cancel();

        mActiveAnimationType = TYPE_BOTH;
        mObjectAnimator.setValues(speakerVolume, seekBarProgress);
        mObjectAnimator.setAutoCancel(true);
        mObjectAnimator.setDuration(400);

        if(distance > 0) {
            mObjectAnimator.start();
        }
    }

    private void animateSpeakerIconOnly(int progress) {
        if(progress == 0 && !mMuted || progress > 0 && mMuted) {
            mObjectAnimator.cancel();

            PropertyValuesHolder speakerVolume = PropertyValuesHolder.ofFloat(
                    "speakerVolume",
                    mSpeakerDrawable.getVolume(),
                    progress > 0 ? progress / 100f : SpeakerDrawable.VOLUME_MUTE
            );

            mActiveAnimationType = TYPE_SPEAKER_ONLY;
            mObjectAnimator.setValues(speakerVolume);
            mObjectAnimator.setDuration(200 + progress);
            mObjectAnimator.start();
            return;
        }

        if(!mObjectAnimator.isRunning()) {
            // Wenn gerade nichts animiert wird, dann können wir einfach so das Drawable updaten.
            mSpeakerDrawable.setVolume(progress / 100f);
        } else {
            // Ansonsten müssen wir die aktuelle Animation abbrechen, um unseren Zielwert neu zu justieren.
            mObjectAnimator.cancel();
        }
    }

    private void updateAndNotifyVolume(int volume) {
        boolean changed = mVolume != volume;
        mVolume = volume;
        if(changed && mListener != null) {
            mListener.onVolumeChange(volume, mMuted);
        }
    }

    private void updateAndNotifyMuted(boolean muted) {
        boolean changed = muted != mMuted;
        mMuted = muted;
        if(changed && mListener != null) {
            mListener.onVolumeChange(mVolume, muted);
        }
    }

    @SuppressWarnings("unused")
    private void setSpeakerVolume(float volume) {
        mSpeakerDrawable.setVolume(volume);
    }

    @SuppressWarnings("unused")
    private void setSeekBarProgress(int progress) {
        mSeekBar.setProgress(progress);
    }

    public void setVolume(int volume) {
        mVolume = volume;
        if(isAttachedToWindow()) {
            animateSpeakerIconAndSeekBar(mMuted, mVolume);
        }
    }

    public void setMuted(boolean muted) {
        mMuted = muted;
        if(isAttachedToWindow()) {
            animateSpeakerIconAndSeekBar(mMuted, mVolume);
        }
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onVolumeChange(int volume, boolean muted);
    }
}
