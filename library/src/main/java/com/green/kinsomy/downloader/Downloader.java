package com.green.kinsomy.downloader;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by kinsomy on 2018/4/10.
 */

public class Downloader {

	public static void singleDownload(Context context, String name, String url) {
		Intent i = new Intent(DownloadService.ADD_DOWNTASK);
		i.setAction(DownloadService.ADD_DOWNTASK);
		i.putExtra("name", name);
		i.putExtra("url", url);
		i.setPackage(DownloadService.PACKAGE);
		context.startService(i);
	}

	public static void multiDownload(Context context, ArrayList<String> names, ArrayList<String> urls) {
		Intent i = new Intent(DownloadService.ADD_DOWNTASK);
		i.setAction(DownloadService.ADD_MULTI_DOWNTASK);
		i.putExtra("names", names);
		i.putExtra("urls", urls);
		i.setPackage(DownloadService.PACKAGE);
		context.startService(i);
	}

	public static void startAllTask(Context context) {
		Intent intent = new Intent(DownloadService.START_ALL_DOWNTASK);
		intent.setPackage(DownloadService.PACKAGE);
		context.startService(intent);
	}

	public static void pauseAllTask(Context context) {
		Intent intent = new Intent(DownloadService.PAUSE_ALLTASK);
		intent.setPackage(DownloadService.PACKAGE);
		context.startService(intent);
	}
}
