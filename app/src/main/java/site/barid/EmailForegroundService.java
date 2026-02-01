package site.barid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

public class EmailForegroundService extends Service {

    private static final String CHANNEL_ID = "barid_service_channel";
    private static final int SERVICE_NOTIFICATION_ID = 999;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final long CHECK_INTERVAL = 60 * 1000; // 1 Minute

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start foreground immediately
        startForeground(SERVICE_NOTIFICATION_ID, getNotification("Checking for new emails..."));

        // Start the periodic check
        startEmailCheckLoop();

        return START_STICKY; // Restart if killed
    }

    private void startEmailCheckLoop() {
        runnable = new Runnable() {
            @Override
            public void run() {
                checkEmails();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(runnable);
    }

    private void checkEmails() {
        if (!AppUtil.isConnected(getApplicationContext())) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AccountManager accountManager = new AccountManager(getApplicationContext());
                    ArrayList<HashMap<String, Object>> accounts = accountManager.getAccounts();

                    for (HashMap<String, Object> account : accounts) {
                        String address = (String) account.get("address");
                        String response = fetchUrl("https://api.driftz.net/emails/" + address + "?limit=1");
                        JSONObject json = new JSONObject(response);
                        if (json.has("result")) {
                            JSONObject result = json.getJSONObject("result");
                            org.json.JSONArray items = result.getJSONArray("items");
                            
                            if (items.length() > 0) {
                                JSONObject latest = items.getJSONObject(0);
                                String currentId = latest.getString("id");
                                String from = latest.getString("fromAddress");
                                String subject = latest.optString("subject", "(No Subject)");

                                String lastId = "";
                                if (account.containsKey("last_email_id")) {
                                    lastId = (String) account.get("last_email_id");
                                } else {
                                    // First time, just save current
                                    updateAccountLastId(accountManager, accounts, account, currentId);
                                    continue;
                                }

                                if (!currentId.equals(lastId)) {
                                    NotificationHelper.showNotification(
                                        getApplicationContext(),
                                        "New Email from " + from,
                                        subject,
                                        address.hashCode()
                                    );
                                    updateAccountLastId(accountManager, accounts, account, currentId);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateAccountLastId(AccountManager mgr, ArrayList<HashMap<String, Object>> accounts, HashMap<String, Object> account, String lastId) {
        int index = accounts.indexOf(account);
        if (index != -1) {
            account.put("last_email_id", lastId);
            mgr.updateAccount(index, account);
        }
    }

    private String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    private Notification getNotification(String content) {
        Intent notificationIntent = new Intent(this, InboxActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Temp Mail Service")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_stat_notify_email)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
