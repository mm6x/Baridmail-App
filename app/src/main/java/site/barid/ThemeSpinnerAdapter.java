package site.barid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ThemeSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private String[] items;
    private int[] icons = {
        R.drawable.ic_mobile_app,   // Follow System
        R.drawable.ic_sun,          // Light Mode
        R.drawable.ic_moon          // Dark Mode
    };

    public ThemeSpinnerAdapter(Context context, String[] items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.spinner_theme_dropdown_item, parent, false);
        }

        ImageView themeIcon = convertView.findViewById(R.id.theme_icon);
        TextView themeName = convertView.findViewById(R.id.theme_name);

        themeName.setText(items[position]);
        themeIcon.setImageResource(icons[position % icons.length]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.spinner_theme_dropdown_item, parent, false);
        }

        ImageView themeIcon = convertView.findViewById(R.id.theme_icon);
        TextView themeName = convertView.findViewById(R.id.theme_name);

        themeName.setText(items[position]);
        themeIcon.setImageResource(icons[position % icons.length]);

        return convertView;
    }
}
