package de.wolfsline.dndtx.receiver;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Arrays;

public class MyNotificationListener extends NotificationListenerService {

    private final String TAG = MyNotificationListener.class.getSimpleName();

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        // Hier können Sie den Code einfügen, um mit der erhaltenen Benachrichtigung zu arbeiten
        Notification notification = sbn.getNotification();
        String packageName = sbn.getPackageName();

        if (!packageName.equals("com.google.android.deskclock")) {
            return;
        }

        if (!(notification.toString().contains("category=alarm") && notification.toString().contains("actions=2"))) {
            return;
        }

        boolean notifyActionOk = true;
        for (Notification.Action s : sbn.getNotification().actions) {
            if (s.title != null) {
                String title = String.valueOf(s.title);
                if (!(title.equals("Schlummern") || title.equals("Stopp"))) {
                    notifyActionOk = false;
                }
            }
        }

        Log.d(TAG, "Wecker klingelt");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (notifyActionOk && prefs.getBoolean("alarm_active", false)) {
            if (prefs.getBoolean("wifi_only_home", true)) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (!wifiInfo.getSSID().equals("\"" + prefs.getString("wifi_name", "") + "\"")) {
                    return;
                }
            }
            String address = prefs.getString("iobroker_ip", "");
            String port = prefs.getString("iobroker_port", "");
            String adapter = prefs.getString("iobroker_adapter", "");
            String path = prefs.getString("iobroker_alarm_path", "");

            if (address.isEmpty() || port.isEmpty() || adapter.isEmpty() || path.isEmpty()) {
                return;
            }

            final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = address + ":" + port + "/set/" + adapter + "." + path + "?value=" + true;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                    }, error -> {
            });

            String urlDelayed = address + ":" + port + "/set/" + adapter + "." + path + "?value=" + false;
            StringRequest stringRequestDelayed = new StringRequest(Request.Method.GET, urlDelayed,
                    response -> {
                    }, error -> {
            });
            queue.add(stringRequest);

            if (prefs.getBoolean("iobroker_alarm_truefalse", false)) {
                // Verzögerte Ausführung nach 2 Sekunden
                handler.postDelayed(() -> {
                    queue.add(stringRequestDelayed);

                }, 2000); // 2000 Millisekunden entsprechen 2 Sekunden
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        // Code hier für Benachrichtigungsereignisse, wenn sie entfernt werden
    }

}
