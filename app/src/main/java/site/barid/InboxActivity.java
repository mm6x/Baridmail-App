package site.barid;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.os.Vibrator;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import org.json.*;
import site.barid.databinding.*;

public class InboxActivity extends AppCompatActivity {
	
	private InboxBinding binding;
	private HashMap<String, Object> countResponse = new HashMap<>();
	private HashMap<String, Object> countResult = new HashMap<>();
	private HashMap<String, Object> emailsResponse = new HashMap<>();
	private HashMap<String, Object> emailDetailResponse = new HashMap<>();
	private HashMap<String, Object> emailDetail = new HashMap<>();
	
	private ArrayList<HashMap<String, Object>> emailList = new ArrayList<>();
	
	private RequestNetwork countRequest;
	private RequestNetwork.RequestListener countRequest_listener;
	private RequestNetwork listRequest;
	private RequestNetwork.RequestListener listRequest_listener;
	private SharedPreferences currentEmail;
	private RequestNetwork detailRequest;
	private RequestNetwork.RequestListener detailRequest_listener;
	private SharedPreferences contents;
	private Vibrator vibrator;
	private SharedPreferences readPrefs;
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save the current navigation item
		if (binding != null && binding.bottomNavigation != null) {
			outState.putInt("nav_item_id", binding.bottomNavigation.getSelectedItemId());
		}
	}
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = InboxBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
		
		// Restore Navigation State
		if (_savedInstanceState != null) {
			int navId = _savedInstanceState.getInt("nav_item_id", R.id.nav_inbox);
			binding.bottomNavigation.setSelectedItemId(navId);
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		countRequest = new RequestNetwork(this);
		listRequest = new RequestNetwork(this);
		currentEmail = getSharedPreferences("emails.json", Activity.MODE_PRIVATE);
		detailRequest = new RequestNetwork(this);
		contents = getSharedPreferences("content.json", Activity.MODE_PRIVATE);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		readPrefs = getSharedPreferences("read_emails", Activity.MODE_PRIVATE);
		
		binding.swiperefreshlayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				vibrator.vibrate((long)(50));
				countRequest.startRequestNetwork(RequestNetworkController.GET, "https://api.barid.site/emails/count/".concat(getIntent().getStringExtra("email").concat(getIntent().getStringExtra("spinnerValue")).replace(" ", "")), getIntent().getStringExtra("email").concat(getIntent().getStringExtra("spinnerValue")).replace(" ", ""), countRequest_listener);
			}
		});
		
		binding.listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				final int _position = _param3;
				
			}
		});
		
		countRequest_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				vibrator.vibrate((long)(50));
				try {
					countResponse = new Gson().fromJson(_response, new TypeToken<HashMap<String, Object>>(){}.getType());
					if (countResponse != null && countResponse.get("result") != null) {
						countResult = new Gson().fromJson(countResponse.get("result").toString(), new TypeToken<HashMap<String, Object>>(){}.getType());
						if (countResult.get("count") != null && countResult.get("count").toString().contains("0.0")) {
							binding.swiperefreshlayout1.setRefreshing(false);
							binding.listlin.setVisibility(View.GONE);
							binding.noemails.setVisibility(View.VISIBLE);
						} else {
							listRequest.startRequestNetwork(RequestNetworkController.GET, "https://api.barid.site/emails/".concat(_tag.concat("?limit=20&offset=0")), _tag, listRequest_listener);
							currentEmail.edit().putString("count", (countResult.get("count") != null ? countResult.get("count").toString() : "0")).commit();
							currentEmail.edit().putString("email", _tag).commit();
							// binding.textview3.setText(_tag); // Removed view
							binding.swiperefreshlayout1.setRefreshing(false);
							binding.noemails.setVisibility(View.GONE);
							binding.listlin.setVisibility(View.VISIBLE);
						}
					}
				} catch (Exception e) {
					AppUtil.showMessage(InboxActivity.this, "Error parsing response");
					binding.swiperefreshlayout1.setRefreshing(false);
				}
			}
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				AppUtil.showMessage(InboxActivity.this, AppUtil.getFriendlyErrorMessage(InboxActivity.this, _message));
				binding.swiperefreshlayout1.setRefreshing(false);
			}
		};
		
		listRequest_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				try{
					emailsResponse = new Gson().fromJson(_response, new TypeToken<HashMap<String, Object>>(){}.getType());
					emailList = new Gson().fromJson(new Gson().toJson(emailsResponse.get("result")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					binding.listview1.setAdapter(new Listview1Adapter(emailList));
					((BaseAdapter)binding.listview1.getAdapter()).notifyDataSetChanged();
				}catch (Exception e) {
				}
			}
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				AppUtil.showMessage(InboxActivity.this, AppUtil.getFriendlyErrorMessage(InboxActivity.this, _message));
			}
		};
		
		detailRequest_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				try {
					emailDetailResponse = new Gson().fromJson(_response, new TypeToken<HashMap<String, Object>>(){}.getType());
					if (emailDetailResponse != null && emailDetailResponse.get("result") != null) {
						Map item = (Map) emailDetailResponse.get("result");
						contents.edit().putString("id", String.valueOf(item.get("id"))).commit();
						contents.edit().putString("text", String.valueOf(item.get("text_content"))).commit();
						contents.edit().putString("from", String.valueOf(item.get("from_address"))).commit();
						contents.edit().putString("subject", String.valueOf(item.get("subject"))).commit();
						Intent detailIntent = new Intent(getApplicationContext(), ViewActivity.class);
						startActivity(detailIntent);
					}
				} catch (Exception e) {
					AppUtil.showMessage(InboxActivity.this, "Error opening email");
				}
			}
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				AppUtil.showMessage(InboxActivity.this, AppUtil.getFriendlyErrorMessage(InboxActivity.this, _message));
			}
		};
	}
	
	private void initializeLogic() {
		// binding.textview3.setVisibility(View.GONE); // Removed view
		binding.listlin.setVisibility(View.GONE);
		binding.noemails.setVisibility(View.GONE); 
		
		// Dark Mode Status Bar Logic: 
		// If in Night Mode -> Clear Light Status Bar flag (Light Text)
		// If in Day Mode -> Set Light Status Bar flag (Dark Text)
		// Dark Mode Status Bar Logic: Preserve existing flags
		int flags = getWindow().getDecorView().getSystemUiVisibility();
		int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
		
		if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
			flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
		} else {
			flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
		}
		getWindow().getDecorView().setSystemUiVisibility(flags);
		
		if (Build.VERSION.SDK_INT >= 33) {
			if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
			}
		}
		
		getWindow().setStatusBarColor(getResources().getColor(R.color.bg_screen));
		
		getWindow().setStatusBarColor(getResources().getColor(R.color.bg_screen));
		
		AccountManager accountManager = new AccountManager(this);
		
		// Migration Logic: Check for legacy session if no accounts exist
		if (!accountManager.hasAccounts()) {
			SharedPreferences legacyPrefs = getSharedPreferences("emails.json", Activity.MODE_PRIVATE);
			String legacyEmail = legacyPrefs.getString("email", "");
			if (!legacyEmail.isEmpty() && legacyEmail.contains("@")) {
				int atIndex = legacyEmail.indexOf("@");
				if (atIndex > 0) {
					String userPart = legacyEmail.substring(0, atIndex);
					String domainPart = legacyEmail.substring(atIndex);
					accountManager.addAccount(userPart, domainPart);
					AppUtil.CustomToast(getApplicationContext(), "Restored saved account", 0xFFFFFFFF, 14, 0xFF4CAF50, 20, AppUtil.BOTTOM);
				}
			}
		}
		
		// Fallback: If accounts exist but none is selected (e.g. error state), auto-select the last one
		if (accountManager.getCurrentAccount() == null && accountManager.hasAccounts()) {
			accountManager.switchToAccount(accountManager.getAccounts().size() - 1);
		}
		
		HashMap<String, Object> currentAccount = accountManager.getCurrentAccount();
		
		if (currentAccount != null) {
			String emailPart = currentAccount.get("email_part").toString();
			String domainPart = currentAccount.get("domain_part").toString();
			String fullAddress = currentAccount.get("address").toString();
			
			binding.tvAvatarProfile.setText(fullAddress.substring(0, 1).toUpperCase());
			
			// Use saved color or generate one if missing
			int color = 0;
			if (currentAccount.containsKey("color")) {
				color = (int) Double.parseDouble(currentAccount.get("color").toString());
			} else {
				color = 0xFF000000 | (fullAddress.hashCode() & 0x00FFFFFF);
			}
			((android.graphics.drawable.GradientDrawable) binding.tvAvatarProfile.getBackground()).setColor(color);
			
			binding.tvAvatarProfile.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AccountsBottomSheetFragment bottomSheet = new AccountsBottomSheetFragment();
					bottomSheet.show(getSupportFragmentManager(), "accountsSheet");
				}
			});
			
			binding.swiperefreshlayout1.setRefreshing(true);
			
			// Save current email params to intent extras for compatibility with existing code if widely used
			getIntent().putExtra("email", emailPart);
			getIntent().putExtra("spinnerValue", domainPart);
			
			countRequest.startRequestNetwork(RequestNetworkController.GET, "https://api.barid.site/emails/count/".concat(fullAddress), fullAddress, countRequest_listener);
		} else {
			Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
			startActivity(intent);
			finish();
		}
		
		// Search Logic
		binding.etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (binding.listview1.getAdapter() != null) {
					((Listview1Adapter)binding.listview1.getAdapter()).filter(s.toString());
				}
			}
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void afterTextChanged(Editable s) {}
		});
		
		// Clear All Logic
		binding.btnClearAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show iOS Confirmation Dialog logic would go here, 
				// but for now let's just trigger the delete request for simplicity as per user "do it" urgency.
				// Ideally we'd show a "Are you sure?" dialog.
				
				final Dialog dialog = new Dialog(InboxActivity.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(true);
				dialog.setContentView(site.barid.R.layout.dialog_ios);
				dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

				TextView tvTitle = dialog.findViewById(site.barid.R.id.tv_title);
				TextView tvMessage = dialog.findViewById(site.barid.R.id.tv_message);
				TextView btnAction = dialog.findViewById(site.barid.R.id.btn_action);

				tvTitle.setText(getString(R.string.dialog_clear_title));
				tvMessage.setText(getString(R.string.dialog_clear_msg));
				btnAction.setText(getString(R.string.dialog_delete));
				btnAction.setTextColor(Color.RED);

				btnAction.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String currentEmail = getIntent().getStringExtra("email") + getIntent().getStringExtra("spinnerValue");
						listRequest.startRequestNetwork(RequestNetworkController.DELETE, "https://api.barid.site/emails/" + currentEmail, "", new RequestNetwork.RequestListener() {
							@Override
							public void onResponse(String tag, String response, HashMap<String, Object> headers) {
								AppUtil.showIOSDialog(InboxActivity.this, "Success", getString(R.string.success_cleared));
								// Refresh list
								countRequest.startRequestNetwork(RequestNetworkController.GET, "https://api.barid.site/emails/count/".concat(getIntent().getStringExtra("email").concat(getIntent().getStringExtra("spinnerValue")).replace(" ", "")), getIntent().getStringExtra("email").concat(getIntent().getStringExtra("spinnerValue")).replace(" ", ""), countRequest_listener);
								dialog.dismiss();
							}
							@Override
							public void onErrorResponse(String tag, String message) {
								AppUtil.showMessage(InboxActivity.this, AppUtil.getFriendlyErrorMessage(InboxActivity.this, message));
								dialog.dismiss();
							}
						});
					}
				});
				dialog.show();
			}
		});

		// Settings Logic
		// Bottom Navigation Logic
		// Bottom Navigation Logic
		binding.bottomNavigation.setOnItemSelectedListener(new com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
				int itemId = item.getItemId();
				if (itemId == R.id.nav_inbox) {
					binding.viewInbox.setVisibility(View.VISIBLE);
					binding.viewSettings.setVisibility(View.GONE);

					binding.listview1.smoothScrollToPosition(0);
					return true;
				} else if (itemId == R.id.nav_add_account) {
					AccountsBottomSheetFragment bottomSheet = new AccountsBottomSheetFragment();
					bottomSheet.show(getSupportFragmentManager(), "accountsSheet");
					return false; // Don't select the button
				} else if (itemId == R.id.nav_settings) {
					binding.viewInbox.setVisibility(View.GONE);
					binding.viewSettings.setVisibility(View.VISIBLE);
					return true;
				}
				return false;
			}
		});
		
		initializeSettings();
	}
	
	public class Listview1Adapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		ArrayList<HashMap<String, Object>> _originalData;
		
		public Listview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
			_originalData = new ArrayList<>(_arr);
		}
		
		public void filter(String query) {
			_data.clear();
			if (query == null || query.isEmpty()) {
				_data.addAll(_originalData);
			} else {
				String lowerQuery = query.toLowerCase().trim();
				for (HashMap<String, Object> item : _originalData) {
					String from = item.get("from_address") != null ? item.get("from_address").toString().toLowerCase() : "";
					String subject = item.get("subject") != null ? item.get("subject").toString().toLowerCase() : "";
					// Also search snippet if available (mocked for now, but good practice)
					String snippet = item.get("text_content") != null ? item.get("text_content").toString().toLowerCase() : "";
					
					if (from.contains(lowerQuery) || subject.contains(lowerQuery) || snippet.contains(lowerQuery)) {
						_data.add(item);
					}
				}
			}
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			EmailsBinding itemBinding;
			if (_v == null) {
				itemBinding = EmailsBinding.inflate(getLayoutInflater());
				_v = itemBinding.getRoot();
				_v.setTag(itemBinding);
			} else {
				itemBinding = (EmailsBinding) _v.getTag();
			}
			
			String from = _data.get((int)_position).get("from_address").toString();
			String subject = _data.get((int)_position).get("subject").toString();
			
			itemBinding.textview1.setText(from); 
			itemBinding.textview2.setText(subject); 
			
			if (from.length() > 0) {
				itemBinding.tvAvatar.setText(from.substring(0, 1).toUpperCase());
				int color = 0xFF000000 | (from.hashCode() & 0x00FFFFFF);
				((android.graphics.drawable.GradientDrawable) itemBinding.tvAvatar.getBackground()).setColor(color);
			}
			
			itemBinding.tvSnippet.setText(getString(R.string.tap_to_view));
			itemBinding.tvDate.setText(getString(R.string.now));
			
			final String currentId = _data.get((int)_position).get("id").toString();
			
			// Unread Logic
			if (readPrefs.contains(currentId)) {
				itemBinding.ivReadIndicator.setVisibility(View.INVISIBLE);
				itemBinding.textview2.setTypeface(null, Typeface.NORMAL);
				itemBinding.textview1.setTypeface(null, Typeface.NORMAL);
			} else {
				itemBinding.ivReadIndicator.setVisibility(View.VISIBLE);
				itemBinding.textview2.setTypeface(null, Typeface.BOLD);
				itemBinding.textview1.setTypeface(null, Typeface.BOLD);
			}

			itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (!readPrefs.contains(currentId)) {
						readPrefs.edit().putBoolean(currentId, true).apply();
						notifyDataSetChanged();
					}
					
					detailRequest.startRequestNetwork(RequestNetworkController.GET, "https://api.barid.site/inbox/".concat(currentId), "A", detailRequest_listener);
				}
			});
			
			return _v;
		}
	}

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

	private void initializeSettings() {
		final SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
		int currentMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        final TextView btnDark = binding.getRoot().findViewById(R.id.btn_mode_dark);
        final TextView btnLight = binding.getRoot().findViewById(R.id.btn_mode_light);
        
        // Initial State
        updateThemeUI(currentMode, btnDark, btnLight);

		btnDark.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                int mode = AppCompatDelegate.MODE_NIGHT_YES;
				prefs.edit().putInt("theme_mode", mode).apply();
				AppCompatDelegate.setDefaultNightMode(mode);
                updateThemeUI(mode, btnDark, btnLight);
			}
		});

        btnLight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                int mode = AppCompatDelegate.MODE_NIGHT_NO;
				prefs.edit().putInt("theme_mode", mode).apply();
				AppCompatDelegate.setDefaultNightMode(mode);
                updateThemeUI(mode, btnDark, btnLight);
			}
		});
		
        // Language Card Logic
        final TextView tvCurrentLang = binding.getRoot().findViewById(R.id.tv_current_language);
        String langCode = LocaleHelper.getLanguage(this);
        if (langCode.equals("ar")) {
             tvCurrentLang.setText(R.string.lang_ar);
        } else if (langCode.equals("de")) {
             tvCurrentLang.setText(R.string.lang_de);
        } else if (langCode.equals("es")) {
             tvCurrentLang.setText(R.string.lang_es);
        } else if (langCode.equals("ru")) {
             tvCurrentLang.setText(R.string.lang_ru);
        } else {
             tvCurrentLang.setText(R.string.lang_en);
        }

        binding.getRoot().findViewById(R.id.card_language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

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

    private void showLanguageDialog() {
        LanguageBottomSheetFragment bottomSheet = new LanguageBottomSheetFragment();
        bottomSheet.show(getSupportFragmentManager(), "languageSheet");
    }

    private void updateThemeUI(int mode, TextView btnDark, TextView btnLight) {
        int activeColor = getResources().getColor(R.color.brand_blue);
        int inactiveColor = getResources().getColor(R.color.text_secondary);
        
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            btnDark.setTextColor(activeColor);
            btnLight.setTextColor(inactiveColor);
        } else {
            btnDark.setTextColor(inactiveColor);
            btnLight.setTextColor(activeColor);
        }
    }
}
