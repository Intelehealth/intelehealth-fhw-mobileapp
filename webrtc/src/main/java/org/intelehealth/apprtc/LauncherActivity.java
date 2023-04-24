package org.intelehealth.apprtc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    public void callNow(View view) {
        EditText editText = findViewById(R.id.room_id_etc);
        String roomId = editText.getText().toString().trim();
        if (roomId.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.enter_room_id), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, CompleteActivity.class).putExtra("roomId", roomId));

    }

    public void chatNow(View view) {
        startActivity(new Intent(this, ChatActivity.class));
    }
}
