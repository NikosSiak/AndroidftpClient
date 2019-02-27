package com.example.nikos.ftpclient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

//public class MainActivity extends AppCompatActivity implements TaskCompleted {
public class MainActivity extends AppCompatActivity{

    private AutoCompleteTextView ipTV;
    private AutoCompleteTextView portTV;
    private AutoCompleteTextView usernameTV;
    private EditText passwordTV;
    private Button loginBT;
    private int ipCounter = 0;
    private int portCounter = 0;
    private int usernameCounter = 0;
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

        final ArrayList<String> ips = readFiles("ip",this);;
        final ArrayList<String> usernames = readFiles("username",this);
        final ArrayList<String> ports = readFiles("port",this);

        if (ips.size() == 1){
            ipTV.setText(ips.get(0));
        }
        if (usernames.size() == 1){
            usernameTV.setText(usernames.get(0));
        }
        if (ports.size() == 1){
            usernameTV.setText(ports.get(0));
        }

        ArrayAdapter<String> ipAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,ips);
        ArrayAdapter<String> usernameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,usernames);
        ArrayAdapter<String> portAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,ports);
        ipTV.setAdapter(ipAdapter);
        usernameTV.setAdapter(usernameAdapter);
        portTV.setAdapter(portAdapter);
        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ips.contains(ipTV.getText().toString())){
                    writeFile("ip",ipTV.getText().toString(),getApplicationContext());
                }
                if (!ports.contains(portTV.getText().toString())){
                    writeFile("port",portTV.getText().toString(),getApplicationContext());
                }
                if (!usernames.contains(usernameTV.getText().toString())){
                    writeFile("username",usernameTV.getText().toString(),getApplicationContext());
                }
                Intent intent = new Intent(MainActivity.this, ServerFiles.class);
                intent.putExtra("IP", ipTV.getText().toString());
                intent.putExtra("PORT", portTV.getText().toString());
                intent.putExtra("USERNAME", usernameTV.getText().toString());
                intent.putExtra("PASSWORD", passwordTV.getText().toString());
                startActivity(intent);
            }
        });
    }

    public void writeFile(String type, String data, Context context){
        String counter;
        if (type.equals("ip")){
            counter = String.valueOf(ipCounter);
            ipCounter++;
        }
        else if (type.equals("port")){
            counter = String.valueOf(portCounter);
            portCounter++;
        }
        else{
            counter = String.valueOf(usernameCounter);
            usernameCounter++;
        }
        try{
            FileOutputStream fileOutputStream = context.openFileOutput(type + counter + ".txt",context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<String> readFiles(String type,Context context){
        ArrayList<String> data = new ArrayList<String>();
        String datum;
        FileInputStream fileInputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        int counter;
        if (type.equals("ip")){
            counter = ipCounter;
        }
        else if (type.equals("port")){
            counter = portCounter;
        }
        else{
            counter = usernameCounter;
        }
        for (int i = 0; i < counter; i++){
            try{
                fileInputStream = context.openFileInput(type + String.valueOf(i) + ".txt");
                inputStreamReader = new InputStreamReader(fileInputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                datum = bufferedReader.readLine();
                Log.d("datum",datum);
                data.add(datum);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return data;
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
