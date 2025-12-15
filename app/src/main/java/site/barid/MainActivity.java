package site.barid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;
import site.barid.databinding.MainBinding;

public class MainActivity extends AppCompatActivity {
	
	private Timer splashTimer = new Timer();
	private MainBinding binding;
	private Vibrator vibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = MainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize();
		initializeLogic();
	}
	
	private void initialize() {
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	private void initializeLogic() {
		// Advanced Animations
		// Reset Views
		binding.logo.setAlpha(0f);
		binding.logo.setScaleX(0.5f);
		binding.logo.setScaleY(0.5f);
		
		binding.tvAppName.setAlpha(0f);
		binding.tvAppName.setTranslationY(50f);
		
		binding.tvTagline.setAlpha(0f);
		binding.tvTagline.setTranslationY(30f);
		
		// Animate Logo
		binding.logo.animate()
			.alpha(1f)
			.scaleX(1f)
			.scaleY(1f)
			.setDuration(800)
			.setInterpolator(new android.view.animation.OvershootInterpolator())
			.start();
			
		// Animate Text
		binding.tvAppName.animate()
			.alpha(1f)
			.translationY(0f)
			.setStartDelay(300)
			.setDuration(600)
			.setInterpolator(new android.view.animation.DecelerateInterpolator())
			.start();
			
		binding.tvTagline.animate()
			.alpha(1f)
			.translationY(0f)
			.setStartDelay(500)
			.setDuration(600)
			.setInterpolator(new android.view.animation.DecelerateInterpolator())
			.start();
			
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		
		TimerTask navigationTask = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (vibrator != null) vibrator.vibrate(10L);
						
						AccountManager accountManager = new AccountManager(getApplicationContext());
						Intent intent;
						
						if (accountManager.hasAccounts()) {
							intent = new Intent(getApplicationContext(), InboxActivity.class);
						} else {
							intent = new Intent(getApplicationContext(), Main2Activity.class);
						}
						
						startActivity(intent);
						overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
						finish();
					}
				});
			}
		};
		splashTimer.schedule(navigationTask, 2500);
		
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}
}