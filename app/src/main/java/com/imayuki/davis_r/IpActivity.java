package com.imayuki.davis_r;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IpActivity extends AppCompatActivity {

    private EditText editText;
    private Button btEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
        editText = (EditText) findViewById(R.id.serverIp);
        btEnter = (Button) findViewById(R.id.btSend);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverIp = editText.getText().toString();
                if (serverIp.isEmpty()) {
                    Toast.makeText(IpActivity.this, "Please, enter ip address...", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent(IpActivity.this, MainActivity.class);
                    intent.putExtra("serverIp", serverIp);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }
}
