package de.wolfsline.dndtx.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class InterruptionFilterReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean notification;
            if (mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALL) {
                // Alle Benachrichtigungen erlaubt
                notification = true;
            } else {
                // Bitte nicht stören - Eingeschränkte benachrichtgung
                notification = false;
            }
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "http://10.0.0.250:8087/set/0_userdata.0.Variable.Ger%C3%A4te.Google_Pixel_6_Pro.Benachrichtigung?value=" + String.valueOf(notification);
            Log.d("RECEIVER", "CAll: " + url);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                    }, error -> {
            });
            queue.add(stringRequest);
        }
    }
}
