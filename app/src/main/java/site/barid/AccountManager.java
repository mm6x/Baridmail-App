package site.barid;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;

public class AccountManager {
    private static final String PREF_NAME = "barid_accounts";
    private static final String KEY_ACCOUNTS = "accounts_list";
    private static final String KEY_CURRENT_INDEX = "current_account_index";

    private SharedPreferences prefs;
    private Gson gson;

    public AccountManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addAccount(String email, String domain, String password) {
        ArrayList<HashMap<String, Object>> accounts = getAccounts();
        
        // Check if exists
        for (HashMap<String, Object> acc : accounts) {
            if (acc.get("address").equals(email + domain)) {
                if (password != null && !password.isEmpty()) {
                    acc.put("password", password);
                    updateAccount(accounts.indexOf(acc), acc);
                }
                switchToAccount(accounts.indexOf(acc));
                return;
            }
        }

        HashMap<String, Object> newAccount = new HashMap<>();
        newAccount.put("address", email + domain); // Full address
        newAccount.put("email_part", email);
        newAccount.put("domain_part", domain);
        if (password != null && !password.isEmpty()) {
            newAccount.put("password", password);
        }
        newAccount.put("color", AppUtil.getRandom(0xFF100000, 0xFFFFFFFF));
        
        accounts.add(newAccount);
        saveAccounts(accounts);
        switchToAccount(accounts.size() - 1);
    }

    public ArrayList<HashMap<String, Object>> getAccounts() {
        String json = prefs.getString(KEY_ACCOUNTS, "[]");
        return gson.fromJson(json, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
    }

    public HashMap<String, Object> getCurrentAccount() {
        ArrayList<HashMap<String, Object>> accounts = getAccounts();
        int index = prefs.getInt(KEY_CURRENT_INDEX, -1);
        if (index >= 0 && index < accounts.size()) {
            return accounts.get(index);
        }
        return null; // No account logged in
    }

    public void switchToAccount(int index) {
        prefs.edit().putInt(KEY_CURRENT_INDEX, index).apply();
    }
    
    public void removeAccount(int index) {
        ArrayList<HashMap<String, Object>> accounts = getAccounts();
        if (index >= 0 && index < accounts.size()) {
            accounts.remove(index);
            saveAccounts(accounts);
            
            // Adjust current index
            int currentIndex = prefs.getInt(KEY_CURRENT_INDEX, -1);
            if (currentIndex >= index) {
                if (currentIndex > 0) currentIndex--;
                else if (accounts.isEmpty()) currentIndex = -1;
            }
            prefs.edit().putInt(KEY_CURRENT_INDEX, currentIndex).apply();
        }
    }


    
    public void updateAccount(int index, HashMap<String, Object> updatedAccount) {
        ArrayList<HashMap<String, Object>> accounts = getAccounts();
        if (index >= 0 && index < accounts.size()) {
            accounts.set(index, updatedAccount);
            saveAccounts(accounts);
        }
    }

    private void saveAccounts(ArrayList<HashMap<String, Object>> accounts) {
        prefs.edit().putString(KEY_ACCOUNTS, gson.toJson(accounts)).apply();
    }
    
    public boolean hasAccounts() {
        return !getAccounts().isEmpty();
    }
}
