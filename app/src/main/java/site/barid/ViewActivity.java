package site.barid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;
import site.barid.databinding.ViewBinding;

public class ViewActivity extends AppCompatActivity {
	
	private ViewBinding binding;
	private SharedPreferences content;
	
	private RequestNetwork requestNetwork;
	private RequestNetwork.RequestListener _requestNetwork_request_listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ViewBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		setSupportActionBar(binding.toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		content = getSharedPreferences("content.json", Activity.MODE_PRIVATE);
		requestNetwork = new RequestNetwork(this);
		
		_requestNetwork_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, java.util.HashMap<String, Object> _param3) {
				AppUtil.showIOSDialog(ViewActivity.this, "Success", "Email deleted."); // Keep simple for now or extract
				finish();
			}
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				if (_param2.contains("401") || _param2.toLowerCase().contains("unauthorized")) {
					AppUtil.showIOSDialog(ViewActivity.this, "Authentication Failed", getString(R.string.error_auth_failed));
				} else {
					AppUtil.showMessage(ViewActivity.this, AppUtil.getFriendlyErrorMessage(ViewActivity.this, _param2));
				}
			}
		};
		
		setupUI();
	}

	private void setPasswordHeader(RequestNetwork rn) {
		AccountManager accountManager = new AccountManager(this);
		HashMap<String, Object> account = accountManager.getCurrentAccount();
		HashMap<String, Object> headers = new HashMap<>();
		if (account != null && account.containsKey("password")) {
			headers.put("x-inbox-password", account.get("password").toString());
		}
		rn.setHeaders(headers);
	}
	
	private void setupUI() {
		String subject = content.getString("subject", "No Subject");
		String from = content.getString("from", "Unknown");
		String body = content.getString("text", "");
		final String id = content.getString("id", "");
		
		binding.tvSubject.setText(subject);
		binding.tvSender.setText(from);
		binding.tvBody.setText(body);
		
		// Avatar Logic
		if (from.length() > 0) {
			binding.tvAvatar.setText(from.substring(0, 1).toUpperCase());
			int color = 0xFF000000 | (from.hashCode() & 0x00FFFFFF);
			((GradientDrawable) binding.tvAvatar.getBackground()).setColor(color);
		}
		
		binding.btnDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!id.isEmpty()) {
					setPasswordHeader(requestNetwork);
					requestNetwork.startRequestNetwork(RequestNetworkController.DELETE, "https://api.driftz.net/inbox/" + id, "", _requestNetwork_request_listener);
				}
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}