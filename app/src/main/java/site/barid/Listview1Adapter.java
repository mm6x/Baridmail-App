package site.barid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

public class Listview1Adapter extends BaseAdapter {
    
    ArrayList<HashMap<String, Object>> data;
    
    public Listview1Adapter(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
    }
    
    @Override
    public int getCount() {
        return data.size();
    }
    
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, null);
        }
        
        TextView textView = convertView.findViewById(android.R.id.text1);
        HashMap<String, Object> item = data.get(position);
        textView.setText(item.toString());
        
        return convertView;
    }
}