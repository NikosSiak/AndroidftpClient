package com.example.nikos.ftpclient;

import org.apache.commons.net.ftp.FTPFile;

public interface TaskCompletedFTPFiles {
    public void onTaskCompleteFtpFiles(FTPFile[] files);
}
