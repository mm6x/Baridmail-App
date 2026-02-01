package site.barid;

import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import site.barid.databinding.Main2Binding;

public class Main2Activity extends AppCompatActivity {
	
	private Main2Binding binding;
	private ArrayList<String> domains = new ArrayList<>();
	
	private RequestNetwork requestNetwork;
	private RequestNetwork.RequestListener _requestNetwork_request_listener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = Main2Binding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		requestNetwork = new RequestNetwork(this);
		_requestNetwork_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> headers) {
				try {
					HashMap<String, Object> map = new com.google.gson.Gson().fromJson(response, new com.google.gson.reflect.TypeToken<HashMap<String, Object>>(){}.getType());
					if (map.containsKey("result")) {
						Map<String, Object> result = (Map<String, Object>) map.get("result");
						if (result.containsKey("public")) {
							ArrayList<String> newDomains = (ArrayList<String>) result.get("public");
							if (newDomains != null && !newDomains.isEmpty()) {
								domains.clear();
								for (String d : newDomains) {
									domains.add("@" + d);
								}
								((ArrayAdapter)binding.spinner1.getAdapter()).notifyDataSetChanged();
							}
						}
					}
				} catch (Exception e) {
					// Fallback to hardcoded defaults (already added)
				}
			}
			@Override
			public void onErrorResponse(String tag, String message) {
				// Fallback to hardcoded defaults
			}
		};
		
		setupDomains();
		setupUI();
		
		// Fetch Dynamic Domains
		requestNetwork.startRequestNetwork(RequestNetworkController.GET, "https://api.driftz.net/domains", "domains", _requestNetwork_request_listener);
	}
	
	private void setupDomains() {
		domains.add("@driftz.net");
		domains.add("@vwh.sh");
		domains.add("@iusearch.lol");
		domains.add("@lifetalk.us");
		domains.add("@z44d.pro");
		domains.add("@wael.fun");
		domains.add("@tawbah.site");
		domains.add("@kuruptd.ink");
		domains.add("@oxno1.space");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, domains);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		binding.spinner1.setAdapter(adapter);
	}
	
	private void setupUI() {
		// Clean User Input on Type
		binding.edittext1.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String text = s.toString();
				String clean = text.replaceAll("[^a-zA-Z0-9]", "");
				if (!text.equals(clean)) {
					binding.edittext1.setText(clean);
					binding.edittext1.setSelection(clean.length());
				}
			}
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void afterTextChanged(Editable s) {}
		});
		
		// Randomize Button Logic
		binding.tilEmail.setEndIconOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String random = java.util.UUID.randomUUID().toString().substring(0, 8);
				binding.edittext1.setText(random);
				binding.edittext1.setSelection(random.length());
			}
		});

		// Password Visibility Logic with Animation
		binding.cbProtected.setOnCheckedChangeListener((buttonView, isChecked) -> {
			android.transition.TransitionManager.beginDelayedTransition((android.view.ViewGroup) binding.getRoot());
			binding.tilPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		});

		// Auto-detection Logic
		AdapterView.OnItemSelectedListener domainListener = new AdapterView.OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { checkInboxStatus(); }
			@Override public void onNothingSelected(AdapterView<?> p) {}
		};
		binding.spinner1.setOnItemSelectedListener(domainListener);

		binding.edittext1.addTextChangedListener(new TextWatcher() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkInboxStatus(); }
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void afterTextChanged(Editable s) {}
		});

		// Login Button Logic
		binding.btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = binding.edittext1.getText().toString();
				String password = binding.etPassword.getText().toString();
				boolean isProtected = binding.cbProtected.isChecked();

				if (email.isEmpty()) {
					binding.tilEmail.setError(getString(R.string.error_username_required));
					return;
				}
				
				binding.tilEmail.setError(null);
				binding.tilPassword.setError(null);
				
				String fullAddress = email + binding.spinner1.getSelectedItem().toString();
				
				// Always check status before proceeding to be safe
				binding.btnLogin.setEnabled(false);
				binding.btnLogin.setText("Checking...");
				
				RequestNetwork checkReq = new RequestNetwork(Main2Activity.this);
				// If we have a password, include it in the header for the check
				if (isProtected && !password.isEmpty()) {
					HashMap<String, Object> headers = new HashMap<>();
					headers.put("x-inbox-password", password);
					checkReq.setHeaders(headers);
				}

				checkReq.startRequestNetwork(RequestNetworkController.GET, "https://api.driftz.net/emails/" + fullAddress + "?limit=1", "final_check", new RequestNetwork.RequestListener() {
					@Override
					public void onResponse(String tag, String response, HashMap<String, Object> headers) {
						binding.btnLogin.setEnabled(true);
						binding.btnLogin.setText(R.string.btn_login);
						try {
							HashMap<String, Object> map = new com.google.gson.Gson().fromJson(response, new com.google.gson.reflect.TypeToken<HashMap<String, Object>>(){}.getType());
							
							if (map.containsKey("success") && (boolean)map.get("success")) {
								if (map.containsKey("result")) {
									Map<String, Object> result = (Map<String, Object>) map.get("result");
									boolean locked = (boolean) result.get("locked");
									
									if (locked && !isProtected) {
										// Oops, it's locked but we didn't know. Force password field.
										binding.cbProtected.setChecked(true);
										binding.tilPassword.setError(getString(R.string.error_password_required));
										return;
									}
									
									// If we are here, either it's not locked, or we had the right password (since 200 OK)
									saveAndProceed(email, binding.spinner1.getSelectedItem().toString(), isProtected ? password : null);
								}
							} else {
								// Success is false
								String error = map.containsKey("error") ? map.get("error").toString() : "Unknown Error";
								if (error.contains("password") || error.contains("Unauthorized")) {
									if (!isProtected) {
										binding.cbProtected.setChecked(true);
										binding.tilPassword.setError(getString(R.string.error_password_required));
									} else {
										AppUtil.showIOSDialog(Main2Activity.this, "Authentication Failed", getString(R.string.error_auth_failed));
									}
								} else {
									AppUtil.showMessage(Main2Activity.this, error);
								}
							}
						} catch (Exception e) {
							AppUtil.showMessage(Main2Activity.this, "Error parsing response");
						}
					}

					@Override
					public void onErrorResponse(String tag, String message) {
						binding.btnLogin.setEnabled(true);
						binding.btnLogin.setText(R.string.btn_login);
						AppUtil.showMessage(Main2Activity.this, AppUtil.getFriendlyErrorMessage(Main2Activity.this, message));
					}
				});
			}
		});
	}

	private void saveAndProceed(String email, String domain, String password) {
		AccountManager accountManager = new AccountManager(Main2Activity.this);
		accountManager.addAccount(email, domain, password);
		Intent intent = new Intent(getApplicationContext(), InboxActivity.class);
		startActivity(intent);
		finish();
	}

	private void checkInboxStatus() {
		String email = binding.edittext1.getText().toString();
		Object selectedItem = binding.spinner1.getSelectedItem();
		if (email.length() < 3 || selectedItem == null) return;
		
		String fullAddress = email + selectedItem.toString();
		RequestNetwork statusCheck = new RequestNetwork(this);
		statusCheck.startRequestNetwork(RequestNetworkController.GET, "https://api.driftz.net/emails/" + fullAddress + "?limit=1", "status_check", new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> headers) {
				try {
					HashMap<String, Object> map = new com.google.gson.Gson().fromJson(response, new com.google.gson.reflect.TypeToken<HashMap<String, Object>>(){}.getType());
					if (map.containsKey("result")) {
						Map<String, Object> result = (Map<String, Object>) map.get("result");
						if (result.containsKey("locked")) {
							boolean isLocked = (boolean) result.get("locked");
							if (isLocked != binding.cbProtected.isChecked()) {
								binding.cbProtected.setChecked(isLocked);
							}
						}
					}
				} catch (Exception e) {}
			}
			@Override public void onErrorResponse(String tag, String message) {}
		});
	}
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}