package com.green.kinsomy.downloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kinsomy on 2018/4/24.
 */

public abstract class AbsDownloadReceiver extends BroadcastReceiver {
	public static final String TASK_DOWNLOADING = "com.green.kinsomy.downloader.taskdownloading";
	public static final String TASK_STARTDOWN = "com.green.kinsomy.downloader.taskstart";
	public static final String TASK_ERROR = "com.green.kinsomy.downloader.taskerror";
	public static final String TASK_COMPLETED = "com.green.kinsomy.downloader.taskcomplete";
	public static final String TASK_CANCEL = "com.green.kinsomy.downloader.taskcancel";
	public static final String TASK_PAUSE = "com.green.kinsomy.downloader.taskpause";

	protected final static String EXTRA_TASK = "task";
	protected final static String EXTRA_MSG = "msg";
	protected final static String EXTRA_CODE = "code";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		switch (action) {
			case TASK_DOWNLOADING:
				DownloadTask downloadingTask = intent.getParcelableExtra(EXTRA_TASK);
				onTaskDownloadingEvent(downloadingTask);
				break;
			case TASK_STARTDOWN:
				DownloadTask startTask = intent.getParcelableExtra(EXTRA_TASK);
				onTaskStartEvent(startTask);
				break;
			case TASK_ERROR:
				DownloadTask errorTask = intent.getParcelableExtra(EXTRA_TASK);
				int code = intent.getIntExtra(EXTRA_CODE,-1);
				onTaskErrorEvent(errorTask,code);
				break;
			case TASK_CANCEL:
				DownloadTask cancelTask = intent.getParcelableExtra(EXTRA_TASK);
				onTaskCancelEvent(cancelTask);
				break;
			case TASK_COMPLETED:
				DownloadTask completedTask = intent.getParcelableExtra(EXTRA_TASK);
				onTaskCompletedEvent(completedTask);
				break;
			case TASK_PAUSE:
				DownloadTask pauseTask = intent.getParcelableExtra(EXTRA_TASK);
				onTaskPauseEvent(pauseTask);
				break;
			default:
				break;
		}
	}

	public abstract void onTaskDownloadingEvent(DownloadTask task);

	public abstract void onTaskStartEvent(DownloadTask task);

	public abstract void onTaskErrorEvent(DownloadTask task, int code);

	public abstract void onTaskCancelEvent(DownloadTask task);

	public abstract void onTaskPauseEvent(DownloadTask task);

	public abstract void onTaskCompletedEvent(DownloadTask task);
}
