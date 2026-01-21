package site.barid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LanguageBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bs_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String currentLang = LocaleHelper.getLanguage(getContext());

        setupLanguageItem(view, R.id.lang_en, R.id.check_en, "en", currentLang);
        setupLanguageItem(view, R.id.lang_ar, R.id.check_ar, "ar", currentLang);
        setupLanguageItem(view, R.id.lang_de, R.id.check_de, "de", currentLang);
        setupLanguageItem(view, R.id.lang_es, R.id.check_es, "es", currentLang);
        setupLanguageItem(view, R.id.lang_ru, R.id.check_ru, "ru", currentLang);
    }

    private void setupLanguageItem(View root, int containerId, int checkId, final String code, String currentLang) {
        View container = root.findViewById(containerId);
        ImageView check = root.findViewById(checkId);

        if (currentLang.equals(code)) {
            check.setVisibility(View.VISIBLE);
        } else {
            check.setVisibility(View.GONE);
        }

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!code.equals(LocaleHelper.getLanguage(getContext()))) {
                    LocaleHelper.setLocale(getContext(), code);
                    restartApp();
                }
                dismiss();
            }
        });
    }

    private void restartApp() {
        Intent intent = new Intent(getContext(), InboxActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
