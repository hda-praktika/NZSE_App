package com.example.nzse.net;

import com.example.external.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TVClient {
    private static final int TIMEOUT = 10000;

    private HttpRequest mHttpRequest;

    public TVClient(String ipAddress) {
        setIPAddress(ipAddress);
    }

    public void setIPAddress(String ipAddress) {
        mHttpRequest = new HttpRequest(ipAddress, TIMEOUT);
    }

    public class Request {
        private Map<String, String> mMap = new HashMap<>();

        private Request() { }

        public Request setDebug(boolean debug) {
            mMap.put("debug", debug ? "1" : "0");
            return this;
        }

        public Request setVolume(int volume) {
            mMap.put("volume", String.valueOf(volume));
            return this;
        }

        public Request setChannelMain(String id) {
            mMap.put("channelMain", id);
            return this;
        }

        public Request setMainZoom(boolean scaled) {
            mMap.put("zoomMain", scaled ? "1" : "0");
            return this;
        }

        public Request setPIP(boolean pip) {
            mMap.put("showPip", pip ? "1" : "0");
            return this;
        }

        public Request setChannelPip(String id) {
            mMap.put("channelPip", id);
            return this;
        }

        public Request setPipZoom(boolean scaled) {
            mMap.put("zoomPip", scaled ? "1" : "0");
            return this;
        }

        public Request pauseProgram() {
            mMap.put("timeShiftPause", "");
            return this;
        }

        public Request playProgram(int shift) {
            mMap.put("timeShiftPlay", String.valueOf(shift));
            return this;
        }

        public Request setStandby(boolean standby) {
            mMap.put("standby", standby ? "1" : "0");
            return this;
        }

        public Request powerOff() {
            mMap.put("powerOff", "");
            return this;
        }

        private String buildQuery() {
            StringBuilder sb = new StringBuilder();

            boolean first = true;
            for(Map.Entry<String, String> entry : mMap.entrySet()) {
                if(!first) {
                    sb.append('&');
                }

                sb.append(entry.getKey());
                sb.append('=');
                sb.append(entry.getValue());

                first = false;
            }

            return sb.toString();
        }

        public void execute() throws IOException, JSONException {
            String query = buildQuery();
            mHttpRequest.sendHttp(query);
        }
    }

    public JSONArray scanChannels() throws IOException, JSONException {
        JSONObject obj = mHttpRequest.sendHttp("scanChannels");
        return obj.getJSONArray("channels");
    }

    public Request newRequest() {
        return new Request();
    }
}
