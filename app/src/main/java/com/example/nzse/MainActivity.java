package com.example.nzse;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.nzse.fragment.SettingsFragment;
import com.example.nzse.fragment.ZoomLevelFragment;
import com.example.nzse.repo.Repository;
import com.example.nzse.util.Callbacks;
import com.example.nzse.viewmodel.MainViewModel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.nzse.util.Callbacks.forwardOnClick;
import static com.example.nzse.util.Callbacks.forwardOnTouch;

public class MainActivity extends FragmentActivity implements ObjectAnimator.AnimatorListener {
    private MainViewModel mViewModel;

    private FrameLayout mContent;
    private FrameLayout mPanel;
    private View mContentChild;
    private View mPanelChild;
    private View mOverlayView;

    private Drawable mContentOriginalBackground;
    private Drawable mPanelOriginalBackground;
    private ColorDrawable mContentOverlay;

    private Bitmap mBitmapContent;
    private Bitmap mBitmapPanel;

    private float mCurrentDrawerState;

    private ProgressDialog mChannelScanDialog;

    private AtomicBoolean mBackPressedFirstTime = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if(!mViewModel.hasRepository()) {
            Repository repository = new Repository(this);
            repository.setIPAddress("192.168.178.45");
            mViewModel.setRepository(repository);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContent = findViewById(R.id.drawer_content);
        mPanel = findViewById(R.id.drawer_panel);
        mOverlayView = findViewById(R.id.view_overlay);

        mContentChild = mContent.getChildAt(0);
        mPanelChild = mPanel.getChildAt(0);

        mContent.setOnTouchListener(forwardOnTouch(this, "suppressTouchEvent"));
        mPanel.setOnTouchListener(forwardOnTouch(this, "suppressTouchEvent"));

        mContentOriginalBackground = mContent.getBackground();
        mPanelOriginalBackground = mPanel.getBackground();

        // Klicken auf das Overlay schließt den Drawer wieder
        mOverlayView.setOnClickListener(forwardOnClick(this, "onClickOverlay"));
        mOverlayView.setOnTouchListener(forwardOnTouch(this, "onTouchOverlay"));

        // Überblendung für den Hauptinhalt, sodass beim Öffnen
        // des Drawers jener abgedunkelt wird.
        mContentOverlay = new ColorDrawable();
        setContentOverlayAlpha(0);
        mOverlayView.setBackground(mContentOverlay);

        mViewModel.getVolumeChangeException().observe(this, Callbacks.forwardObserver(this, "onVolumeChangeException"));
        mViewModel.isPoweredOff().observe(this, Callbacks.forwardObserver(this, "onPoweredOff"));
        mViewModel.getPowerOffException().observe(this, Callbacks.forwardObserver(this, "onPowerOffException"));
        mViewModel.isRefreshingChannels().observe(this, Callbacks.<Boolean>forwardObserver(this, "onRefreshingChannelsChanged"));

        // Unter der Statusleiste zeichnen
        // Siehe: https://stackoverflow.com/a/29281284/6338257
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // Komplett transparente Statusleiste
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mViewModel.reloadState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mViewModel.syncTV();
    }

    public void openSettings() {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, 0, 0, android.R.anim.fade_out);
        tx.add(R.id.fragment_content, new SettingsFragment());
        tx.addToBackStack(null);
        tx.commit();
    }

    public void openZoomLevel() {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, 0, 0, android.R.anim.fade_out);
        tx.add(R.id.fragment_content, new ZoomLevelFragment());
        tx.addToBackStack(null);
        tx.commit();
    }

    public void openDrawer() {
        animateDrawerState(1);
    }

    public void closeDrawer() {
        animateDrawerState(0);
    }

    @SuppressWarnings("unused")
    public void onClickOverlay(View v) {
        closeDrawer();
    }

    @SuppressWarnings("unused")
    public boolean onTouchOverlay(View v, MotionEvent ev) {
        if(mCurrentDrawerState > 0) {
            return v.onTouchEvent(ev);
        } else {
            return mContent.dispatchTouchEvent(ev);
        }
    }

    /**
     * Spezifiert, wie weit der Drawer offen ist.
     * @param state 0 = geschlossen, 1 = offen
     */
    @SuppressWarnings("unused")
    private void setDrawerState(float state) {
        mCurrentDrawerState = state;

        int panelWidth = mPanel.getWidth();
        mContent.setTranslationX(state * -panelWidth);
        mPanel.setTranslationX((1 - state) * (panelWidth / 2f));
        mOverlayView.setTranslationX(state * -panelWidth);
        setContentOverlayAlpha((int) (state * 80));
    }

    private void animateDrawerState(float to) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(MainActivity.this, "drawerState", mCurrentDrawerState, to);
        anim.addListener(MainActivity.this);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setAutoCancel(true);
        anim.start();
    }

    private void setContentOverlayAlpha(int alpha) {
        mContentOverlay.setColor(Color.BLACK);
        mContentOverlay.setAlpha(alpha);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if(mCurrentDrawerState > 0) {
            closeDrawer();
        } else {
            if(!mBackPressedFirstTime.getAndSet(true)) {
                Toast.makeText(this, R.string.back_twice, Toast.LENGTH_LONG).show();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mBackPressedFirstTime.getAndSet(false);
                    }
                }, 3500);
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onVolumeChangeException(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unused")
    public void onPoweredOff(Void v) {
        finish();
    }

    @SuppressWarnings("unused")
    public void onPowerOffException(Exception e) {
        e.printStackTrace();
        Toast.makeText(this, R.string.power_off_failed, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unused")
    public void onRefreshingChannelsChanged(boolean refreshing) {
        if(refreshing && mChannelScanDialog == null) {
            mChannelScanDialog = ProgressDialog.show(this, "", getString(R.string.scanning_channels), true);
        } else if(!refreshing && mChannelScanDialog != null) {
            mChannelScanDialog.dismiss();
            mChannelScanDialog = null;
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        // Wenn die Drawer-Animation startet, dann rendern wir die aktuellen Inhalte
        // in Bitmaps, die während der Animation dargestellt werden. Dies erhöht die Performance,
        // da wir uns ein aufwändiges Rendern in jedem Frame sparen.

        // Bitmap für Content rendern
        mBitmapContent = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas contentCanvas = new Canvas(mBitmapContent);
        mContentChild.layout(0, 0, mContent.getWidth(), mContent.getHeight());
        mContentChild.draw(contentCanvas);

        // Bitmap für Panel rendern
        mBitmapPanel = Bitmap.createBitmap(mPanel.getWidth(), mPanel.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas panelCanvas = new Canvas(mBitmapPanel);
        mPanelChild.layout(0, 0, mPanel.getWidth(), mPanel.getHeight());
        mPanelChild.draw(panelCanvas);

        // Child Views mit Bitmaps ersetzen
        mContent.setBackground(new BitmapDrawable(getApplicationContext().getResources(), mBitmapContent));
        mContent.removeAllViews();
        mPanel.setBackground(new BitmapDrawable(getApplicationContext().getResources(), mBitmapPanel));
        mPanel.removeAllViews();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        // Hier werden die Bitmaps vom Start wieder verworfen
        // und die Views wiederhergestellt.
        mContent.setBackground(mContentOriginalBackground);
        mContent.addView(mContentChild);
        mPanel.setBackground(mPanelOriginalBackground);
        mPanel.addView(mPanelChild);

        if(mBitmapContent != null) mBitmapContent.recycle();
        if(mBitmapPanel != null) mBitmapPanel.recycle();
    }

    @Override
    public void onAnimationCancel(Animator animation) { }

    @Override
    public void onAnimationRepeat(Animator animation) { }

    @SuppressWarnings("unused")
    public boolean suppressTouchEvent(View v, MotionEvent ev) {
        return true;
    }
}
