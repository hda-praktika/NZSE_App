package com.example.nzse.repo;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.nzse.model.Channel;
import com.example.nzse.model.Volume;
import com.example.nzse.net.TVClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class Repository {
    private static final String TAG = Repository.class.getSimpleName();

    private static final String KEY_IP = "ip";

    // Volume management
    private static final String KEY_VOLUME = "volume";
    private static final String KEY_MUTED = "muted";

    // Channel management
    private static final String KEY_CHANNELS = "channels";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_RECENT_CHANNELS = "recentChannels";
    private static final String KEY_PIP_ENABLED = "pipEnabled";
    private static final String KEY_PRIMARY_CHANNEL = "primaryChannel";
    private static final String KEY_PRIMARY_ZOOM = "primaryZoom";
    private static final String KEY_SECONDARY_CHANNEL = "secondaryChannel";
    private static final String KEY_SECONDARY_ZOOM = "secondaryZoom";

    // Time management
    private static final String KEY_PLAYING = "playing";
    private static final String KEY_TIMESHIFT = "timeshift";

    // Power management
    private static final String KEY_STANDBY = "standby";

    private TVClient mTVClient;
    private SharedPreferences mSharedPreferences;

    public Repository(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(
                mContext.getPackageName(),
                Context.MODE_PRIVATE
        );

        String ip = getIPAddress();
        mTVClient = new TVClient(ip.isEmpty() ? "" : ip);

        mSharedPreferences.getString(KEY_IP, "");

        loadInitialState();
    }

    public synchronized String getIPAddress() {
        return mSharedPreferences.getString(KEY_IP, "");
    }

    public synchronized void setIPAddress(String ip) {
        mTVClient.setIPAddress(ip);
        mSharedPreferences.edit()
                .putString(KEY_IP, ip)
                .apply();
    }

    private synchronized void loadInitialState() {
        mVolume.volume = mSharedPreferences.getInt(KEY_VOLUME, mVolume.volume);
        mVolume.muted = mSharedPreferences.getBoolean(KEY_MUTED, mVolume.muted);
        mPipEnabled = mSharedPreferences.getBoolean(KEY_PIP_ENABLED, mPipEnabled);
        mPrimaryChannel = mSharedPreferences.getString(KEY_PRIMARY_CHANNEL, mPrimaryChannel);
        mPrimaryZoom = mSharedPreferences.getBoolean(KEY_PRIMARY_ZOOM, mPrimaryZoom);
        mSecondaryChannel = mSharedPreferences.getString(KEY_SECONDARY_CHANNEL, mSecondaryChannel);
        mSecondaryZoom = mSharedPreferences.getBoolean(KEY_SECONDARY_ZOOM, mSecondaryZoom);
        mPlaying = mSharedPreferences.getBoolean(KEY_PLAYING, mPlaying);
        mTimeShift = mSharedPreferences.getInt(KEY_TIMESHIFT, mTimeShift);
        mStandby = mSharedPreferences.getBoolean(KEY_STANDBY, mStandby);

        try {
            readChannelListFromSharedPreferences();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            readFavoriteChannelsFromSharedPreferences();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            readRecentChannelsFromSharedPreferences();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendCompleteState() throws IOException, JSONException {
        TVClient.Request req = mTVClient.newRequest()
                .setVolume(mVolume.muted ? 0 : mVolume.volume)
                .setPIP(mPipEnabled)
                .setChannelMain(mPipEnabled ? mSecondaryChannel : mPrimaryChannel)
                .setMainZoom(mPipEnabled ? mSecondaryZoom : mPrimaryZoom)
                .setChannelPip(mPipEnabled ? mPrimaryChannel : mSecondaryChannel)
                .setPipZoom(mPipEnabled ? mPrimaryZoom : mSecondaryZoom)
                .setStandby(mStandby);

        if(mPlaying) {
            req.playProgram(mTimeShift);
        } else {
            req.pauseProgram();
        }

        req.execute();
    }

    // ===================
    //  Volume management
    // ===================
    private Volume mVolume = new Volume(100, false);

    public void setVolume(Volume volume) throws IOException, JSONException {
        mTVClient.newRequest()
                .setVolume(volume.muted ? 0 : volume.volume)
                .execute();

        mVolume = volume;
        mSharedPreferences.edit()
                .putBoolean(KEY_MUTED, volume.muted)
                .putInt(KEY_VOLUME, volume.volume)
                .apply();
    }

    // ====================
    //  Channel management
    // ====================
    private List<Channel> mChannelList = new ArrayList<>(Arrays.asList(
            // Initiale Liste, kann zur Laufzeit aktualisiert werden.
            new Channel("37a", "ARD", "Das Erste"),
            new Channel("37b", "ARD", "hr-fernsehen"),
            new Channel("22a", "ZDFmobil", "ZDF")
    ));
    private List<String> mFavoriteChannels = new ArrayList<>(Arrays.asList(
            "37a",
            "22a",
            "37b"
    ));
    private List<String> mRecentChannels = new ArrayList<>();
    private boolean mPipEnabled = false;
    private String mPrimaryChannel = "37a";
    private boolean mPrimaryZoom = false;
    private String mSecondaryChannel = "22a";
    private boolean mSecondaryZoom = false;

    public synchronized void scanChannels() throws IOException, JSONException {
        JSONArray arr = mTVClient.scanChannels();
        mChannelList = parseChannelScanResponse(arr);

        mChannelList.sort(new Comparator<Channel>() {
            @Override
            public int compare(Channel o1, Channel o2) {
                return o2.quality - o1.quality;
            }
        });
        final Set<String> checked = new HashSet<>();
        mChannelList.removeIf(new Predicate<Channel>() {
            @Override
            public boolean test(Channel channel) {
                if(!checked.contains(channel.program)) {
                    checked.add(channel.program);
                    return false;
                }
                return true;
            }
        });
        mChannelList.sort(new Comparator<Channel>() {
            @Override
            public int compare(Channel o1, Channel o2) {
                int lengthDiff = o1.id.length() - o2.id.length();
                if(lengthDiff != 0) {
                    return lengthDiff;
                }

                return o1.id.compareTo(o2.id);
            }
        });

        mSharedPreferences.edit()
                .putString(KEY_CHANNELS, arr.toString())
                .apply();
    }

    public synchronized List<Channel> getChannelList() {
        return mChannelList;
    }

    public synchronized void addFavoriteChannel(String channelId) {
        if(mFavoriteChannels.contains(channelId)) return;

        mFavoriteChannels.add(channelId);
        writeFavoriteChannelsToSharedPreferences();
    }

    public synchronized void removeFavoriteChannel(String channelId) {
        mFavoriteChannels.remove(channelId);
        writeFavoriteChannelsToSharedPreferences();
    }

    public synchronized List<String> getFavoriteChannels() {
        return mFavoriteChannels;
    }

    public synchronized List<String> getRecentChannels() {
        return mRecentChannels;
    }

    private synchronized void setRecentChannel(String channelId) {
        int idx = mRecentChannels.indexOf(channelId);
        if(idx >= 0) {
            mRecentChannels.remove(idx);
        }
        mRecentChannels.add(0, channelId);

        // Speichern
        JSONArray arr = new JSONArray();
        for(String channel : mRecentChannels) {
            arr.put(channel);
        }
        mSharedPreferences.edit()
                .putString(KEY_RECENT_CHANNELS, arr.toString())
                .apply();
    }

    public synchronized String getActiveChannel() {
        return mPipEnabled ? mSecondaryChannel : mPrimaryChannel;
    }

    public synchronized String getBackgroundChannel() {
        return mPipEnabled ? mPrimaryChannel : null;
    }

    public synchronized void setActiveChannel(String id) throws IOException, JSONException {
        mTVClient.newRequest()
                .setChannelMain(id)
                .execute();

        setRecentChannel(id);
        if(mPipEnabled) {
            mSecondaryChannel = id;
            mSharedPreferences.edit()
                    .putString(KEY_SECONDARY_CHANNEL, id)
                    .apply();
        } else {
            mPrimaryChannel = id;
            mSharedPreferences.edit()
                    .putString(KEY_PRIMARY_CHANNEL, id)
                    .apply();
        }
    }

    public synchronized boolean getActiveChannelZoom() {
        return mPipEnabled ? mSecondaryZoom : mPrimaryZoom;
    }

    public synchronized void setActiveChannelZoom(boolean scaled) throws IOException, JSONException {
        mTVClient.newRequest()
                .setMainZoom(scaled)
                .execute();

        if(mPipEnabled) {
            mSecondaryZoom = scaled;
            mSharedPreferences.edit()
                    .putBoolean(KEY_SECONDARY_ZOOM, scaled)
                    .apply();
        } else {
            mPrimaryZoom = scaled;
            mSharedPreferences.edit()
                    .putBoolean(KEY_PRIMARY_ZOOM, scaled)
                    .apply();
        }
    }

    public synchronized boolean isPipEnabled() {
        return mPipEnabled;
    }

    public synchronized void enablePip(boolean enabled) throws IOException, JSONException {
        String mainChannel = enabled ? mSecondaryChannel : mPrimaryChannel;
        boolean mainZoom = enabled ? mSecondaryZoom : mPrimaryZoom;
        String pipChannel = enabled ? mPrimaryChannel : mSecondaryChannel;
        boolean pipZoom = enabled ? mPrimaryZoom : mSecondaryZoom;

        mTVClient.newRequest()
                .setPIP(enabled)
                .setChannelMain(mainChannel)
                .setMainZoom(mainZoom)
                .setChannelPip(pipChannel)
                .setPipZoom(pipZoom)
                .execute();

        mPipEnabled = enabled;
        mSharedPreferences.edit()
                .putBoolean(KEY_PIP_ENABLED, enabled)
                .apply();
    }

    // =================
    //  Time management
    // =================
    private boolean mPlaying = true;
    private int mTimeShift = 0;

    public boolean isPlaying() {
        return mPlaying;
    }

    public int getTimeShift() {
        return mTimeShift;
    }

    public synchronized void setPlaying(boolean playing) throws IOException, JSONException {
        if(playing) {
            mTVClient.newRequest()
                    .playProgram(mTimeShift)
                    .execute();
        } else {
            mTVClient.newRequest()
                    .pauseProgram()
                    .execute();
        }

        mPlaying = playing;
        mSharedPreferences.edit()
                .putBoolean(KEY_PLAYING, mPlaying)
                .apply();
    }

    private synchronized int getMaxTimeShift() {
        return 99;
    }

    public synchronized boolean canApplyTimeShiftDelta(int delta) {
        int newTimeShift = mTimeShift + delta;
        return newTimeShift >= 0 && newTimeShift <= getMaxTimeShift();
    }

    public synchronized void applyTimeShiftDelta(int delta) throws IOException, JSONException {
        if(!canApplyTimeShiftDelta(delta)) return;

        if(mPlaying) {
            mTVClient.newRequest()
                    .playProgram(mTimeShift + delta)
                    .execute();
        }

        mTimeShift += delta;
        mSharedPreferences.edit()
                .putInt(KEY_TIMESHIFT, mTimeShift)
                .apply();
    }

    public synchronized void live() throws IOException, JSONException {
        mTVClient.newRequest()
                .playProgram(0)
                .execute();

        mTimeShift = 0;
        mPlaying = true;
        mSharedPreferences.edit()
                .putInt(KEY_TIMESHIFT, 0)
                .putBoolean(KEY_PLAYING, true)
                .apply();
    }

    // ==================
    //  Power management
    // ==================
    private boolean mStandby = false;

    public synchronized boolean isStandby() {
        return mStandby;
    }

    public synchronized void setStandby(boolean standby) throws IOException, JSONException {
        mTVClient.newRequest()
                .setStandby(standby)
                .execute();

        mStandby = standby;
        mSharedPreferences.edit()
                .putBoolean(KEY_STANDBY, standby)
                .apply();
    }

    public synchronized void powerOff() throws IOException, JSONException {
        mTVClient.newRequest()
                .powerOff()
                .execute();
    }

    // =========
    //  Helpers
    // =========
    private List<Channel> parseChannelScanResponse(JSONArray arr) throws JSONException {
        List<Channel> list = new ArrayList<>();
        for(int i = 0, len = arr.length(); i < len; i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.getString("channel");
            String program = obj.getString("program");
            String provider = obj.getString("provider");
            int quality = obj.getInt("quality");
            list.add(new Channel(id, provider, program, quality));
        }
        return list;
    }

    private synchronized void readChannelListFromSharedPreferences() throws JSONException {
        String jsonStr = mSharedPreferences.getString(KEY_CHANNELS, "");
        if(jsonStr.isEmpty()) return;

        mChannelList = parseChannelScanResponse(new JSONArray(jsonStr));
    }

    private synchronized void writeFavoriteChannelsToSharedPreferences() {
        JSONArray arr = new JSONArray();
        for(String channelId : mFavoriteChannels) {
            arr.put(channelId);
        }
        String jsonStr = arr.toString();
        mSharedPreferences.edit()
                .putString(KEY_FAVORITES, jsonStr)
                .apply();
    }

    private synchronized void readFavoriteChannelsFromSharedPreferences() throws JSONException {
        String jsonStr = mSharedPreferences.getString(KEY_FAVORITES, "");
        if(jsonStr.isEmpty()) return;

        JSONArray arr = new JSONArray(jsonStr);
        List<String> list = new ArrayList<>();
        for(int i = 0, len = arr.length(); i < len; i++) {
            list.add(arr.getString(i));
        }
        mFavoriteChannels = list;
    }

    private synchronized void readRecentChannelsFromSharedPreferences() throws JSONException {
        String jsonStr = mSharedPreferences.getString(KEY_RECENT_CHANNELS, "");
        if(jsonStr.isEmpty()) return;

        JSONArray arr = new JSONArray(jsonStr);
        List<String> list = new ArrayList<>();
        for(int i = 0, len = arr.length(); i < len; i++) {
            list.add(arr.getString(i));
        }
        mRecentChannels = list;
    }
}
