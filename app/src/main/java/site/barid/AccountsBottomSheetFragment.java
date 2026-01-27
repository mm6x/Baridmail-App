package site.barid;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.HashMap;

public class AccountsBottomSheetFragment extends BottomSheetDialogFragment {

    private AccountManager accountManager;
    private AccountsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bs_accounts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accountManager = new AccountManager(requireContext());
        RecyclerView rvAccounts = view.findViewById(R.id.rv_accounts);
        View btnAddAccount = view.findViewById(R.id.btn_add_account);

        adapter = new AccountsAdapter(accountManager.getAccounts());
        rvAccounts.setAdapter(adapter);

        btnAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Main2Activity.class);
                startActivity(intent);
                dismiss();
            }
        });
    }

    private class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {
        private ArrayList<HashMap<String, Object>> accounts;

        public AccountsAdapter(ArrayList<HashMap<String, Object>> accounts) {
            this.accounts = accounts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            HashMap<String, Object> account = accounts.get(position);
            String address = account.get("address").toString();
            
            holder.tvEmail.setText(address);
            holder.tvAvatar.setText(address.substring(0, 1).toUpperCase());
            
            int color = 0;
            if (account.containsKey("color")) {
                color = (int) Double.parseDouble(account.get("color").toString());
            } else {
                color = 0xFF000000 | (address.hashCode() & 0x00FFFFFF);
            }
            ((GradientDrawable) holder.tvAvatar.getBackground()).setColor(color);

            HashMap<String, Object> current = accountManager.getCurrentAccount();
            if (current != null && current.get("address").equals(address)) {
                // Sho w indicator for currently selected account
                holder.ivIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.ivIndicator.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> current = accountManager.getCurrentAccount();
                    boolean isCurrentAccount = current != null && current.get("address").equals(address);
                    
                    if (!isCurrentAccount) {
                        accountManager.switchToAccount(position);
                        // Reload Inbox
                        Intent intent = new Intent(getContext(), InboxActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    dismiss();
                }
            });
            
            holder.ivCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Email Address", address);
                    clipboard.setPrimaryClip(clip);
                    AppUtil.showMessage(getActivity(), "Email copied to clipboard");
                }
            });

             holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Quick delete
                    if (accounts.size() > 1) {
                        accountManager.removeAccount(position);
                       // Reload Inbox
                        Intent intent = new Intent(getContext(), InboxActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        dismiss();
                    } else {
                         AppUtil.showMessage(getActivity(), "Cannot remove last account. Use Add Account first.");
                    }
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return accounts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvEmail;
            ImageView ivCopy, ivIndicator;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAvatar = itemView.findViewById(R.id.tv_avatar);
                tvEmail = itemView.findViewById(R.id.tv_email);
                ivCopy = itemView.findViewById(R.id.iv_copy);
                ivIndicator = itemView.findViewById(R.id.iv_indicator);
            }
        }
    }
}
