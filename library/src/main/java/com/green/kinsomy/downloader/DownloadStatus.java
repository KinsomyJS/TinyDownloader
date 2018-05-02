package com.green.kinsomy.downloader;

/**
 * Created by kinsomy on 2018/4/9.
 */
public class DownloadStatus {
    public static final int DOWNLOAD_STATUS_INIT = -1;
    public static final int DOWNLOAD_STATUS_PREPARE = 0;
    public static final int DOWNLOAD_STATUS_START = 1;
    public static final int DOWNLOAD_STATUS_DOWNLOADING = 2;
    public static final int DOWNLOAD_STATUS_DOWNLOADING_WITHOUT_PROGRESS = 3;
    public static final int DOWNLOAD_STATUS_CANCEL = 4;
    public static final int DOWNLOAD_STATUS_ERROR = 5;
    public static final int DOWNLOAD_STATUS_COMPLETED = 6;
    public static final int DOWNLOAD_STATUS_PAUSE = 7;

}
