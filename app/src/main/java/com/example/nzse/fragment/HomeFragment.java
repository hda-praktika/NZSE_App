package com.example.nzse.fragment;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.nzse.MainActivity;
import com.example.nzse.R;
import com.example.nzse.drawable.GlowDrawable;
import com.example.nzse.model.Channel;
import com.example.nzse.util.Callbacks;
import com.example.nzse.viewmodel.MainViewModel;
import com.example.nzse.widget.ActionButton;
import com.example.nzse.widget.ChannelControl;
import com.example.nzse.widget.IconSwitch;
import com.example.nzse.widget.TimeControl;
import com.example.nzse.widget.Toolbar;
import com.example.nzse.widget.VolumeControl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.nzse.util.Callbacks.forwardObserver;
import static com.example.nzse.util.Callbacks.forwardOnClick;
import static com.example.nzse.util.Callbacks.forwardOnLongClick;

public class HomeFragment extends Fragment implements VolumeControl.Listener, TimeControl.Listener, IconSwitch.OnToggleListener, ChannelControl.Listener {
    private MainViewModel mViewModel;

    private Toolbar mToolbar;
    private IconSwitch mIconSwitch;
    private ChannelControl mChannelControl;
    private VolumeControl mVolumeControl;
    private TimeControl mTimeControl;

    private ActionButton mActionButtonPower;
    private ActionButton mActionButtonSettings;

    private GlowDrawable mGlowDrawable;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mToolbar = root.findViewById(R.id.toolbar);
        mIconSwitch = root.findViewById(R.id.iconSwitch);
        mChannelControl = root.findViewById(R.id.channelControl);
        mVolumeControl = root.findViewById(R.id.volumeControl);
        mTimeControl = root.findViewById(R.id.timeControl);
        mActionButtonPower = mToolbar.getActionButton();
        mActionButtonSettings = mToolbar.findViewById(R.id.actionButton_settings);

        mIconSwitch.setOnToggleListener(this);
        mChannelControl.setListener(this);
        mActionButtonPower.setIcon(R.drawable.ic_power);
        mActionButtonPower.setOnClickListener(forwardOnClick(this, "onClickPower"));
        mActionButtonPower.setOnLongClickListener(forwardOnLongClick(this, "onLongClickPower"));
        mActionButtonSettings.setOnClickListener(forwardOnClick(this, "onClickSettings"));
        mVolumeControl.setListener(this);
        mTimeControl.setListener(this);

        mViewModel.isPipEnabled().observe(this, forwardObserver(this, "onPipEnabledChanged"));
        mViewModel.isPlaying().observe(this, forwardObserver(this, "onPlayingChanged"));
        mViewModel.isLive().observe(this, forwardObserver(this, "onLiveChanged"));
        mViewModel.isShiftBackEnabled().observe(this, forwardObserver(this, "onShiftBackEnabledChanged"));
        mViewModel.isShiftForwardEnabled().observe(this, forwardObserver(this, "onShiftForwardEnabledChanged"));
        mViewModel.isStandby().observe(this, forwardObserver(this, "onStandbyChanged"));
        mViewModel.getFrontChannels().observe(this, forwardObserver(this, "onFrontChannelsChanged"));
        mViewModel.getActiveFrontChannel().observe(this, forwardObserver(this, "onActiveFrontChannelChanged"));
        mViewModel.getBackgroundFrontChannel().observe(this, forwardObserver(this, "onBackgroundFrontChannelChanged"));
        mViewModel.getActiveChannel().observe(this, forwardObserver(this, "onActiveChannelChanged"));
        mViewModel.getFavoriteChannels().observe(this, Callbacks.<List<String>>forwardObserver(this, "onFavoriteChannelsChanged"));

        mGlowDrawable = (GlowDrawable) mActionButtonPower.getIcon();

        return root;
    }

    @SuppressWarnings("unused")
    public void onClickPower(View v) {
        mViewModel.toggleStandby();
    }

    @SuppressWarnings("unused")
    public boolean onLongClickPower(View v) {
        mViewModel.powerOff();
        return true;
    }

    @SuppressWarnings("unused")
    public void onClickSettings(View v) {
        MainActivity activity = (MainActivity) requireActivity();
        activity.openSettings();
    }

    @Override
    public void onVolumeChange(int volume, boolean muted) {
        mViewModel.setVolume(volume, muted);
    }

    @Override
    public void onClickLive() {
        mViewModel.live();
    }

    @Override
    public void onClickPlay() {
        mViewModel.togglePlayback();
    }

    @Override
    public void onClickShiftBack() {
        mViewModel.shift(MainViewModel.ShiftDirection.BACK);
    }

    @Override
    public void onClickShiftForward() {
        mViewModel.shift(MainViewModel.ShiftDirection.FORWARD);
    }

    @SuppressWarnings("unused")
    public void onPlayingChanged(boolean playing) {
        mTimeControl.setPlaying(playing);
    }

    @SuppressWarnings("unused")
    public void onLiveChanged(boolean live) {
        mTimeControl.setLive(live);
    }

    @SuppressWarnings("unused")
    public void onShiftBackEnabledChanged(boolean enabled) {
        mTimeControl.setShiftBackEnabled(enabled);
    }

    @SuppressWarnings("unused")
    public void onShiftForwardEnabledChanged(boolean enabled) {
        mTimeControl.setShiftForwardEnabled(enabled);
    }

    @SuppressWarnings("unused")
    public void onPipEnabledChanged(boolean pipEnabled) {
        mIconSwitch.setToggled(pipEnabled);
    }

    @SuppressWarnings("unused")
    public void onStandbyChanged(boolean standby) {
        ObjectAnimator anim = ObjectAnimator.ofInt(mGlowDrawable, "alpha", mGlowDrawable.getAlpha(), standby ? 0 : 255);
        anim.setAutoCancel(true);
        anim.start();
    }

    @Override
    public void onToggle(boolean pipEnabled) {
        mViewModel.setPipEnabled(pipEnabled);
    }

    @Override
    public void onClickChannel(int index) {
        if(index == ChannelControl.BUTTON_MORE) {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openDrawer();
        } else {
            mViewModel.selectFrontChannel(index);
        }
    }

    @Override
    public void onClickPreviousChannel() {
        mViewModel.selectPreviousChannel();
    }

    @Override
    public void onClickNextChannel() {
        mViewModel.selectNextChannel();
    }

    private static final Map<String, Integer> sChannelIcons = new HashMap<>();
    static {
        sChannelIcons.put("8a", R.drawable.ic_channel_phoenix);
        sChannelIcons.put("8b", R.drawable.ic_channel_br);
        sChannelIcons.put("8c", R.drawable.ic_channel_swr);
        sChannelIcons.put("22a", R.drawable.ic_channel_zdf);
        sChannelIcons.put("22b", R.drawable.ic_channel_3sat);
        sChannelIcons.put("22c", R.drawable.ic_channel_zdfinfo);
        sChannelIcons.put("22d", R.drawable.ic_channel_kika);
        sChannelIcons.put("30a", R.drawable.ic_channel_3sat);
        sChannelIcons.put("30b", R.drawable.ic_channel_zdf);
        sChannelIcons.put("30c", R.drawable.ic_channel_zdfinfo);
        sChannelIcons.put("30d", R.drawable.ic_channel_kika);
        sChannelIcons.put("34a", R.drawable.ic_channel_rtl);
        sChannelIcons.put("34b", R.drawable.ic_channel_rtl2);
        sChannelIcons.put("34c", R.drawable.ic_channel_superrtl);
        sChannelIcons.put("34d", R.drawable.ic_channel_vox);
        sChannelIcons.put("37a", R.drawable.ic_channel_ard);
        sChannelIcons.put("37b", R.drawable.ic_channel_hr);
        sChannelIcons.put("37c", R.drawable.ic_channel_arte);
        sChannelIcons.put("44a", R.drawable.ic_channel_br);
        sChannelIcons.put("44b", R.drawable.ic_channel_hr);
        sChannelIcons.put("44c", R.drawable.ic_channel_swr);
        sChannelIcons.put("44d", R.drawable.ic_channel_wdr);
        sChannelIcons.put("54a", R.drawable.ic_channel_kabeleins);
        sChannelIcons.put("54b", R.drawable.ic_channel_n24);
        sChannelIcons.put("54c", R.drawable.ic_channel_prosieben);
        sChannelIcons.put("54d", R.drawable.ic_channel_sat1);
        sChannelIcons.put("57a", R.drawable.ic_channel_arte);
        sChannelIcons.put("57b", R.drawable.ic_channel_phoenix);
        sChannelIcons.put("57c", R.drawable.ic_channel_einsplus);
        sChannelIcons.put("57d", R.drawable.ic_channel_ard);
        sChannelIcons.put("64a", R.drawable.ic_channel_tele5);
        sChannelIcons.put("64b", R.drawable.ic_channel_eurosport);
        sChannelIcons.put("64c", R.drawable.ic_channel_rmtv);
    }

    private Drawable getChannelIcon(String channelId) {
        Integer resId = sChannelIcons.get(channelId);
        if(resId != null) {
            return getContext().getDrawable(resId);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public void onFrontChannelsChanged(List<String> channels) {
        for(int i = 0; i < ChannelControl.BUTTON_MORE; i++) {
            if(i < channels.size()) {
                mChannelControl.setChannelIcon(i, getChannelIcon(channels.get(i)));
            } else {
                mChannelControl.setChannelIcon(i, null);
            }
        }
    }

    @Override
    public void onClickFavorite() {
        mViewModel.toggleCurrentChannelFavorite();
    }

    @SuppressWarnings("unused")
    public void onActiveChannelChanged(Channel channel) {
        mChannelControl.setActiveProvider(channel.provider);
        mChannelControl.setActiveProgram(channel.program);

        boolean isFavorite = mViewModel.getFavoriteChannels().getValue().contains(channel.id);
        mChannelControl.setFavorite(isFavorite, false);
    }

    @SuppressWarnings("unused")
    public void onActiveFrontChannelChanged(int index) {
        mChannelControl.setActiveChannel(index);
    }

    @SuppressWarnings("unused")
    public void onBackgroundFrontChannelChanged(int index) {
        mChannelControl.setBackgroundChannel(index);
    }

    @SuppressWarnings("unused")
    public void onFavoriteChannelsChanged(List<String> favoriteChannels) {
        boolean isFavorite = favoriteChannels.contains(mViewModel.getActiveChannel().getValue());
        mChannelControl.setFavorite(isFavorite, false);
    }
}
