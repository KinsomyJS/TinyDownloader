package com.green.kinsomy.downloader;

import android.util.Log;

/**
 * Created by kinsomy on 2018/4/9.
 */
public class MusesLog {
	public static void D(boolean print, String tag, String content) {
		if (print)
			Log.d(tag, content);
	}

	public static void E(boolean print, String tag, String content) {
		if (print)
			Log.e(tag, content);
	}
}
