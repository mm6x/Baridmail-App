package site.barid;

import android.content.Intent;
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
						ArrayList<String> newDomains = new com.google.gson.Gson().fromJson(new com.google.gson.Gson().toJson(map.get("result")), new com.google.gson.reflect.TypeToken<ArrayList<String>>(){}.getType());
						if (newDomains != null && !newDomains.isEmpty()) {
							domains.clear();
							for (String d : newDomains) {
								domains.add("@" + d);
							}
							((ArrayAdapter)binding.spinner1.getAdapter()).notifyDataSetChanged();
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
		requestNetwork.startRequestNetwork(RequestNetworkController.GET, "https://api.barid.site/domains", "domains", _requestNetwork_request_listener);
	}
	
	private void setupDomains() {
		domains.add("@barid.site");
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
		
		// Login Button Logic
		binding.btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = binding.edittext1.getText().toString();
				if (email.isEmpty()) {
					binding.tilEmail.setError("Username required");
				} else {
					binding.tilEmail.setError(null);
					
					// Save Account using Manager
					AccountManager accountManager = new AccountManager(Main2Activity.this);
					accountManager.addAccount(email, binding.spinner1.getSelectedItem().toString());
					
					Intent intent = new Intent(getApplicationContext(), InboxActivity.class);
					// No need to pass extras anymore, Inbox will read from Manager
					startActivity(intent);
					finish(); // Close login screen
				}
			}
		});
	}
}