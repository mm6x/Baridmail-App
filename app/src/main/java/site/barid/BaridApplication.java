package site.barid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class BaridApplication extends Application {
    
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply Theme
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("error", Log.getStackTraceString(ex));
                startActivity(intent);
                System.exit(1);
            }
        });
        
        scheduleEmailCheck();
    }

    private void scheduleEmailCheck() {
        try {
            Intent serviceIntent = new Intent(this, EmailForegroundService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                     startForegroundService(serviceIntent);
                } catch (Exception e) {
                     // Fallback or permission restricted
                }
            } else {
                startService(serviceIntent);
            }
        } catch (Exception e) {
            // Service start failed
        }
    }
}
