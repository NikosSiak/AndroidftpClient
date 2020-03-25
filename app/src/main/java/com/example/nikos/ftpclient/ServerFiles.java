package com.example.nikos.ftpclient;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

public class ServerFiles extends AppCompatActivity implements TaskCompletedFTPFiles {

    ListView filesListView;
    String ip;
    String port;
    String username;
    String password;
    FTPClient ftp = new FTPClient();
    filesAdapter fa;
    FTPFile[] files;
    String lastCall = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_files);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        ip = getIntent().getStringExtra("IP");
        port = getIntent().getStringExtra("PORT");
        username = getIntent().getStringExtra("USERNAME");
        password = getIntent().getStringExtra("PASSWORD");

        filesListView = findViewById(R.id.filesListView);

        new FtpClient(ServerFiles.this, "login", "").execute();

        FloatingActionButton upload = (FloatingActionButton) findViewById(R.id.uploadButton);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastCall = "upload";
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });
    }

    @Override
    public void onTaskCompleteFtpFiles(final FTPFile[] f) {
        files = f;
        if (lastCall.equals("download")){
            Snackbar.make(getWindow().getDecorView().getRootView(),"Download Complete", Snackbar.LENGTH_SHORT).show();
        }
        else if (lastCall.equals("upload")){
            Snackbar.make(getWindow().getDecorView().getRootView(),"Upload Complete", Snackbar.LENGTH_SHORT).show();
        }
        fa = new filesAdapter(this, R.layout.items_show, files);
        filesListView.setAdapter(fa);
        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FTPFile file = (FTPFile) filesListView.getItemAtPosition(position);
                if (file.isDirectory()) {
                    lastCall = "cwd";
                    new FtpClient(ServerFiles.this, "cwd", file.getName()).execute();
                    //fa.notifyDataSetChanged();
                } else if (file.isFile()) {
                    lastCall = "download";
                    new FtpClient(ServerFiles.this, "download", file.getName()).execute();
                }
            }
        });
    }

    class FtpClient extends AsyncTask<Void, Void, FTPFile[]> {

        private TaskCompletedFTPFiles mCallback;
        private String function;
        private String filename;
        private Uri uri;

        public FtpClient(Context context, String function, String filename) {
            this.mCallback = (TaskCompletedFTPFiles) context;
            this.function = function;
            this.filename = filename;
        }

        public FtpClient(Context context, String function, Uri uri) {
            this.mCallback = (TaskCompletedFTPFiles) context;
            this.function = function;
            this.uri = uri;
        }

        @Override
        protected FTPFile[] doInBackground(Void... voids) {
            FTPFile[] files;
            try {
                files = ftp.listFiles();
            } catch (IOException e) {
                e.printStackTrace();
                files = new FTPFile[0];
            }
            if (function.equals("login")) {
                try {
                    int reply;
                    ftp.connect(ip, Integer.parseInt(port));
                    reply = ftp.getReplyCode();

                    if (!FTPReply.isPositiveCompletion(reply)) {
                        ftp.disconnect();
                        Toast.makeText(getApplicationContext(), "Error Connecting to Server", Toast.LENGTH_SHORT).show();
                    }
                    ftp.login(username, password);
                    ftp.setControlEncoding("UTF-8");
                    //ftp.setFileType(BINARY_FILE_TYPE);
                    files = ftp.listFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error Connecting to Server", Toast.LENGTH_SHORT).show();
                    files = new FTPFile[0];
                }
            } else if (function.equals("cwd")) {
                try {
                    ftp.cwd(filename);
                    files = ftp.listFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                    files = new FTPFile[0];
                }
            } else if (function.equals("download")) {
                if (isExternalStorageWritable()) {
                    try {
                        ftp.setFileType(BINARY_FILE_TYPE);
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", filename);
                        OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
                        ftp.retrieveFile(filename, fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (function.equals("upload")){
                try{
                    ftp.setFileType(BINARY_FILE_TYPE);
                    InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(this.uri);
                    ftp.storeFile(getFileName(uri), inputStream);
                    inputStream.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                files = new FTPFile[0];
            }
            return files;
        }

        @Override
        protected void onPostExecute(FTPFile[] files) {
            mCallback.onTaskCompleteFtpFiles(files);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        lastCall = "cwd";
        new FtpClient(ServerFiles.this, "cwd", "..").execute();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case 7:
                if (resultCode==RESULT_OK){
                    Uri uri = data.getData();
                    lastCall = "upload";
                    new FtpClient(ServerFiles.this,"upload", uri).execute();
                }
                break;
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}