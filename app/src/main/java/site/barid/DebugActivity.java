package site.barid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class DebugActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String error = getIntent().getStringExtra("error");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("An error occurred");
        builder.setMessage(error);
        builder.setPositiveButton("End Application", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();
    }
}
