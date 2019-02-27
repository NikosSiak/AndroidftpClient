package com.example.nikos.ftpclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.nikos.ftpclient.R;

import org.apache.commons.net.ftp.FTPFile;

public class filesAdapter extends ArrayAdapter<FTPFile> {

    public filesAdapter(Context context, int resource,FTPFile[] files){
        super(context, resource, files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.items_show,null);
        }
        FTPFile file = getItem(position);

        if (file != null){
            TextView fileName = convertView.findViewById(R.id.fileName);
            //TextView fileSize = convertView.findViewById(R.id.fileSize);

            fileName.setText(file.getName()+'\n');
            //fileSize.setText(Long.toString(file.getSize()));
        }
        return convertView;
    }
}
