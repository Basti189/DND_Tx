package de.wolfsline.dndtx.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class InterruptionFilterReceiver extends BroadcastReceiver {

    private final static String TAG = InterruptionFilterReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean notification;
            if (mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALL) {
                notification = true;
            } else {
                notification = false;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("active", false)) {
                return;
            }

            if (prefs.getBoolean("wifi_only_home", true)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (!wifiInfo.getSSID().equals("\"" + prefs.getString("wifi_name", "") + "\"")) {
                    return;
                }
            }

            String address = prefs.getString("iobroker_ip", "");
            String port = prefs.getString("iobroker_port", "");
            String adapter = prefs.getString("iobroker_adapter", "");
            String path = prefs.getString("iobroker_path", "");

            if (address.isEmpty() || port.isEmpty() || adapter.isEmpty() || path.isEmpty()) {
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(context);
            String url = address + ":" + port + "/set/" + adapter + "." + path + "?value=" + String.valueOf(notification);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                    }, error -> {
            });
            queue.add(stringRequest);
        }
    }
}
