package com.green.kinsomy.downloader;

/**
 * Created by dzc on 15/11/21.
 */
public interface DownloadTaskListener {
    void onPrepare(DownloadTask downloadTask);

    void onStart(DownloadTask downloadTask);

    void onDownloading(DownloadTask downloadTask);

    void onPause(DownloadTask downloadTask);

    void onCancel(DownloadTask downloadTask);

    void onCompleted(DownloadTask downloadTask);

    void onError(DownloadTask downloadTask, int errorCode);

    int DOWNLOAD_ERROR_FILE_NOT_FOUND = 0x01;
    int DOWNLOAD_ERROR_IO_ERROR = 0x02;
    int DOWNLOAD_ERROR_HTTP_ERROR = 0x03;

}
