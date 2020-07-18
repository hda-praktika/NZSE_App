package com.example.nzse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.external.SingleLiveEvent;
import com.example.nzse.model.Channel;
import com.example.nzse.model.Volume;
import com.example.nzse.repo.Repository;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainViewModel extends ViewModel {
    private static final int MAX_FAVORITES = 3;
    private static final int MAX_RECENT = 2;
    private static final int SHIFT_BACK = 10;
    private static final int SHIFT_FORWARD = -30;

    private Repository mRepository;

    public MainViewModel() {
        mRefreshingChannels.setValue(false);
    }

    public void setRepository(Repository repository) {
        mRepository = repository;

        mChannels.setValue(mRepository.getChannelList());
        mFavoriteChannels.setValue(mRepository.getFavoriteChannels());
    }

    public Repository getRepository() {
        return mRepository;
    }

    public boolean hasRepository() {
        return getRepository() != null;
    }

    // ------------------------------------------------------------------------

    public void syncTV() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepository.sendCompleteState();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void reloadState() {
        mChannels.postValue(mRepository.getChannelList());
        mFavoriteChannels.postValue(mRepository.getFavoriteChannels());
        // mSelectedChannel.postValue(mRepository.get);
        updateFrontChannels();
        mPipEnabled.postValue(mRepository.isPipEnabled());
        // volume
        updateLiveState();
        updatePlaybackState();
        updateShiftEnabledStates();
        mStandby.postValue(mRepository.isStandby());
    }

    // ------------------------------------------------------------------------

    private MutableLiveData<Boolean> mRefreshingChannels = new MutableLiveData<>();
    private SingleLiveEvent<Exception> mChannelScanException = new SingleLiveEvent<>();
    private MutableLiveData<List<Channel>> mChannels = new MutableLiveData<>();
    private SingleLiveEvent<Void> mTooManyFavoriteChannels = new SingleLiveEvent<>();
    private MutableLiveData<List<String>> mFavoriteChannels = new MutableLiveData<>();

    public LiveData<Boolean> isRefreshingChannels() {
        return mRefreshingChannels;
    }

    public void refreshChannels() {
        mRefreshingChannels.postValue(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepository.scanChannels();
                    mChannels.postValue(mRepository.getChannelList());
                } catch (IOException | JSONException e) {
                    mChannelScanException.postValue(e);
                } finally {
                    mRefreshingChannels.postValue(false);
                    updateFrontChannels();
                }
            }
        }).start();
    }

    public void toggleCurrentChannelFavorite() {
        toggleFavoriteChannel(mRepository.getActiveChannel());
    }

    public void toggleFavoriteChannel(String channelId) {
        if(mRepository.getFavoriteChannels().contains(channelId)) {
            mRepository.removeFavoriteChannel(channelId);
        } else {
            if(mRepository.getFavoriteChannels().size() >= MAX_FAVORITES){
                mTooManyFavoriteChannels.postValue(null);
                return;
            }

            mRepository.addFavoriteChannel(channelId);
        }
        mFavoriteChannels.postValue(mRepository.getFavoriteChannels());
        updateFrontChannels();
    }

    public LiveData<List<Channel>> getChannels() {
        return mChannels;
    }

    public SingleLiveEvent<Void> getTooManyFavoriteChannels() {
        return mTooManyFavoriteChannels;
    }

    public LiveData<List<String>> getFavoriteChannels() {
        return mFavoriteChannels;
    }

    public SingleLiveEvent<Exception> getChannelScanException() {
        return mChannelScanException;
    }

    // ------------------------------------------------------------------------

    private MutableLiveData<String> mSelectedChannel = new MutableLiveData<>();
    private MutableLiveData<Integer> mSelectedFrontChannel = new MutableLiveData<>();
    private MutableLiveData<List<String>> mFrontChannels = new MutableLiveData<>();
    private MutableLiveData<Channel> mActiveChannel = new MutableLiveData<>();
    private MutableLiveData<Integer> mActiveFrontChannel = new MutableLiveData<>();
    private MutableLiveData<Integer> mBackgroundFrontChannel = new MutableLiveData<>();
    private SingleLiveEvent<Exception> mZoomLevelException = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> mPipEnabled = new MutableLiveData<>();
    private SingleLiveEvent<Exception> mPipEnableException = new SingleLiveEvent<>();

    public void selectChannel(final String channelId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!Objects.equals(mRepository.getBackgroundChannel(), channelId)) {
                        mRepository.setActiveChannel(channelId);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    mSelectedChannel.postValue(channelId);
                    updateFrontChannels();
                }
            }
        }).start();
    }

    public void selectFrontChannel(int index) {
        List<String> list = buildFrontChannels();
        if(list.size() <= index) {
            return;
        }
        selectChannel(list.get(index));
    }

    private void selectChannelAtOffset(int offset) {
        int activeIndex = mRepository.getChannelList().stream().map(new Function<Channel, String>() {
            @Override
            public String apply(Channel channel) {
                return channel.id;
            }
        }).collect(Collectors.toList()).indexOf(mRepository.getActiveChannel());

        if(activeIndex >= 0) {
            activeIndex += offset;
            int length = mRepository.getChannelList().size();
            String newId = mRepository.getChannelList().get(Math.floorMod(activeIndex, length)).id;
            selectChannel(newId);
        }
    }

    public void selectPreviousChannel() {
        selectChannelAtOffset(-1);
    }

    public void selectNextChannel() {
        selectChannelAtOffset(+1);
    }

    public LiveData<String> getSelectedChannel() {
        return mSelectedChannel;
    }

    public LiveData<Integer> getSelectedFrontChannel() {
        return mSelectedFrontChannel;
    }

    public LiveData<List<String>> getFrontChannels() {
        return mFrontChannels;
    }

    public SingleLiveEvent<Exception> getZoomLevelException() {
        return mZoomLevelException;
    }

    public LiveData<Channel> getActiveChannel() {
        return mActiveChannel;
    }

    public LiveData<Integer> getActiveFrontChannel() {
        return mActiveFrontChannel;
    }

    public LiveData<Integer> getBackgroundFrontChannel() {
        return mBackgroundFrontChannel;
    }

    public void setZoomLevel(final boolean scaled) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepository.setActiveChannelZoom(scaled);
                } catch (IOException | JSONException e) {
                    mZoomLevelException.postValue(e);
                }
            }
        }).start();
    }

    public LiveData<Boolean> isPipEnabled() {
        return mPipEnabled;
    }

    public SingleLiveEvent<Exception> getPipEnableException() {
        return mPipEnableException;
    }

    public void setPipEnabled(final boolean pipEnabled) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> frontChannels = buildFrontChannels();
                    frontChannels.remove(mRepository.getActiveChannel());
                    mRepository.enablePip(pipEnabled);
                    if(pipEnabled) {
                        mRepository.setActiveChannel(frontChannels.get(0));
                    }
                } catch (IOException | JSONException e) {
                    mPipEnableException.postValue(e);
                } finally {
                    mPipEnabled.postValue(mRepository.isPipEnabled());
                    updateFrontChannels();
                }
            }
        }).start();
    }

    private List<String> mPreviousFrontChannels;
    private List<String> buildFrontChannels() {
        List<String> list = new ArrayList<>();
        list.addAll(mRepository.getFavoriteChannels());

        for(String recentChannel : mRepository.getRecentChannels()) {
            if(list.size() >= (MAX_FAVORITES + MAX_RECENT)) {
                break;
            }

            if(!list.contains(recentChannel)) {
                list.add(recentChannel);
            }
        }

        if(mPreviousFrontChannels != null && list.containsAll(mPreviousFrontChannels) && mPreviousFrontChannels.containsAll(list)) {
            return new ArrayList<>(mPreviousFrontChannels);
        }

        mPreviousFrontChannels = new ArrayList<>(list);
        return list;
    }

    private void updateFrontChannels() {
        List<String> list = buildFrontChannels();
        int indexActive = list.indexOf(mRepository.getActiveChannel());
        int indexBackground = mRepository.isPipEnabled() ? list.indexOf(mRepository.getBackgroundChannel()) : -1;

        Optional<Channel> activeChannel = mRepository.getChannelList().stream().filter(new Predicate<Channel>() {
            @Override
            public boolean test(Channel channel) {
                return channel.id.equals(mRepository.getActiveChannel());
            }
        }).findFirst();

        mFrontChannels.postValue(list);
        if(indexActive >= 0) mActiveFrontChannel.postValue(indexActive);
        mBackgroundFrontChannel.postValue(indexBackground);
        if(activeChannel.isPresent()) {
            mActiveChannel.postValue(activeChannel.get());
        }
    }

    // ------------------------------------------------------------------------

    private SingleLiveEvent<Exception> mVolumeChangeException = new SingleLiveEvent<>();

    public void setVolume(final Volume volume) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepository.setVolume(volume);
                } catch (IOException | JSONException e) {
                    mVolumeChangeException.postValue(e);
                }
            }
        }).start();
    }

    public void setVolume(int volume, boolean muted) {
        setVolume(new Volume(volume, muted));
    }

    public SingleLiveEvent<Exception> getVolumeChangeException() {
        return mVolumeChangeException;
    }

    // ------------------------------------------------------------------------

    private MutableLiveData<Boolean> mPlaying = new MutableLiveData<>();
    private SingleLiveEvent<Exception> mTogglePlaybackException = new SingleLiveEvent<>();
    private SingleLiveEvent<Exception> mTimeShiftException = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> mLive = new MutableLiveData<>();
    private SingleLiveEvent<Exception> mLiveException = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> mShiftBackEnabled = new MutableLiveData<>();
    private MutableLiveData<Boolean> mShiftForwardEnabled = new MutableLiveData<>();

    public LiveData<Boolean> isPlaying() {
        return mPlaying;
    }

    public SingleLiveEvent<Exception> getTogglePlaybackException() {
        return mTogglePlaybackException;
    }

    public void togglePlayback() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean newPaused = !mRepository.isPlaying();
                try {
                    mRepository.setPlaying(newPaused);
                } catch (IOException | JSONException e) {
                    mTogglePlaybackException.postValue(e);
                } finally {
                    updatePlaybackState();
                    updateLiveState();
                    updateShiftEnabledStates();
                }
            }
        }).start();
    }

    private void updatePlaybackState() {
        mPlaying.postValue(mRepository.isPlaying());
    }

    public SingleLiveEvent<Exception> getTimeShiftException() {
        return mTimeShiftException;
    }

    public enum ShiftDirection {
        BACK,
        FORWARD
    }

    public void shift(final ShiftDirection dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(dir == ShiftDirection.BACK) {
                        mRepository.applyTimeShiftDelta(SHIFT_BACK);
                    } else {
                        mRepository.applyTimeShiftDelta(SHIFT_FORWARD);
                    }
                } catch (JSONException | IOException e) {
                    mTimeShiftException.postValue(e);
                } finally {
                    updatePlaybackState();
                    updateLiveState();
                    updateShiftEnabledStates();
                }
            }
        }).start();
    }

    public LiveData<Boolean> isLive() {
        return mLive;
    }

    public SingleLiveEvent<Exception> getLiveException() {
        return mLiveException;
    }

    public void live() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepository.live();
                } catch (IOException | JSONException e) {
                    mLiveException.postValue(e);
                } finally {
                    updatePlaybackState();
                    updateLiveState();
                    updateShiftEnabledStates();
                }
            }
        }).start();
    }

    private void updateLiveState() {
        boolean isLive = mRepository.isPlaying() && mRepository.getTimeShift() == 0;
        mLive.postValue(isLive);
    }

    public LiveData<Boolean> isShiftBackEnabled() {
        return mShiftBackEnabled;
    }

    public LiveData<Boolean> isShiftForwardEnabled() {
        return mShiftForwardEnabled;
    }

    private void updateShiftEnabledStates() {
        mShiftBackEnabled.postValue(mRepository.canApplyTimeShiftDelta(SHIFT_BACK));
        mShiftForwardEnabled.postValue(mRepository.canApplyTimeShiftDelta(SHIFT_FORWARD));
    }

    // ------------------------------------------------------------------------

    private SingleLiveEvent<Void> mPoweredOff = new SingleLiveEvent<>();
    private SingleLiveEvent<Exception> mPowerOffException = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> mStandby = new MutableLiveData<>();
    private SingleLiveEvent<Exception> mStandbyException = new SingleLiveEvent<>();

    public SingleLiveEvent<Void> isPoweredOff() {
        return mPoweredOff;
    }

    public SingleLiveEvent<Exception> getPowerOffException() {
        return mPowerOffException;
    }

    public void powerOff() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepository.powerOff();
                    mPoweredOff.postValue(null);
                } catch (IOException | JSONException e) {
                    mPowerOffException.postValue(e);
                }
            }
        }).start();
    }

    public LiveData<Boolean> isStandby() {
        return mStandby;
    }

    public SingleLiveEvent<Exception> getStandbyException() {
        return mStandbyException;
    }

    public void toggleStandby() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean newStandby = !mRepository.isStandby();
                try {
                    mRepository.setStandby(newStandby);
                    mStandby.postValue(newStandby);
                } catch (IOException | JSONException e) {
                    mStandbyException.postValue(e);
                }
            }
        }).start();
    }
}
