package site.barid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import site.barid.databinding.SettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private SettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        
        setupToolbar();
        setupThemeLogic();
        setupAboutSection();
        
        // Status Bar Logic
        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_screen));
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupThemeLogic() {
        int currentMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.rbLight.setChecked(true);
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.rbDark.setChecked(true);
        } else {
            binding.rbSystem.setChecked(true);
        }

        binding.rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                
                if (checkedId == R.id.rb_light) {
                    mode = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (checkedId == R.id.rb_dark) {
                    mode = AppCompatDelegate.MODE_NIGHT_YES;
                }
                
                prefs.edit().putInt("theme_mode", mode).apply();
                AppCompatDelegate.setDefaultNightMode(mode);
            }
        });
    }

    private void setupAboutSection() {
        binding.cardGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mm6x"));
                startActivity(browserIntent);
            }
        });

        binding.cardGithubYazan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vwh/temp-mail"));
                startActivity(browserIntent);
            }
        });
    }
}
