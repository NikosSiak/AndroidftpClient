package com.example.nikos.ftpclient;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


//public class MainActivity extends AppCompatActivity implements TaskCompleted {
public class MainActivity extends AppCompatActivity{

    private EditText ipTV;
    private EditText portTV;
    private EditText usernameTV;
    private EditText passwordTV;
    private Button loginBT;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        verifyStoragePermissions(MainActivity.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipTV = findViewById(R.id.ip);
        portTV = findViewById(R.id.port);
        usernameTV = findViewById(R.id.username);
        passwordTV = findViewById(R.id.password);
        loginBT = findViewById(R.id.loginButton);

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ServerFiles.class);
                intent.putExtra("IP", ipTV.getText().toString());
                intent.putExtra("PORT", portTV.getText().toString());
                intent.putExtra("USERNAME", usernameTV.getText().toString());
                intent.putExtra("PASSWORD", passwordTV.getText().toString());
                startActivity(intent);
            }
        });
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
