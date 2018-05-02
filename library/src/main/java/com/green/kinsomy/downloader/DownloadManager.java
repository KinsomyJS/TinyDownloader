package com.green.kinsomy.downloader;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.green.kinsomy.downloader.db.DbHelper;
import com.green.kinsomy.downloader.db.DownloadDBEntity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by kinsomy on 2018/4/20.
 */

public class DownloadManager {

	public static final String PACKAGE = "com.green.kinsomy.muses";
	private boolean d = true;
	private final String TAG = "DownloadManager";
	private DbHelper mDbHelper;
	private ExecutorService mExecutorService;
	private Map<String, DownloadTask> mCurrentTasks = new HashMap<>();
	private Context mContext;
	private DownloadTaskListener mListener = new DownloadTaskListener() {
		@Override
		public void onPrepare(DownloadTask downloadTask) {
			MusesLog.D(d, TAG, TAG + " task onPrepare");
		}

		@Override
		public void onStart(DownloadTask downloadTask) {
			MusesLog.D(d, TAG, TAG + " task onStart size= " + mCurrentTasks.size());
			sendBroadcast(AbsDownloadReceiver.TASK_STARTDOWN, downloadTask);
		}

		@Override
		public void onDownloading(DownloadTask downloadTask) {
			MusesLog.D(d, TAG, TAG + " task onDownloading");
			sendBroadcast(AbsDownloadReceiver.TASK_DOWNLOADING, downloadTask);
		}

		@Override
		public void onPause(DownloadTask downloadTask) {
			MusesLog.D(d, TAG, TAG + " task onPause");
			sendBroadcast(AbsDownloadReceiver.TASK_PAUSE, downloadTask);
		}

		@Override
		public void onCancel(DownloadTask downloadTask) {
			MusesLog.D(d, TAG, TAG + " task onCancel");
			sendBroadcast(AbsDownloadReceiver.TASK_CANCEL, downloadTask);
		}

		@Override
		public void onCompleted(DownloadTask downloadTask) {
			MusesLog.D(d, TAG, TAG + " task Completed");
			MusesLog.D(d, TAG, "complete task and start");
			sendBroadcast(AbsDownloadReceiver.TASK_COMPLETED, downloadTask);
		}

		@Override
		public void onError(DownloadTask downloadTask, int errorCode) {
			MusesLog.D(d, TAG, TAG + " task onError");
			sendBroadcast(AbsDownloadReceiver.TASK_ERROR, downloadTask);
		}
	};

	public DownloadManager(Context context) {
		this.mContext = context;
		this.mExecutorService = new PriorityExecutor(5, false);
		this.mDbHelper = DbHelper.getInstance(context.getApplicationContext());
	}

	private void sendBroadcast(String action, DownloadTask task) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(AbsDownloadReceiver.EXTRA_TASK, task);
		intent.setPackage(PACKAGE);
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	}

	private String getDownSave(String dir) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(dir);
			if (!file.exists()) {
				boolean r = file.mkdirs();
				if (!r) {
					return null;
				}
				return file.getAbsolutePath() + "/";
			}
			return file.getAbsolutePath() + "/";
		} else {
			return null;
		}
	}

	public DownloadTask addDownloadTask(String dir, String name, String url, String id) {
		//是否自定义id
		if (id.isEmpty()) {
			id = url.hashCode() + "";
		}
		if (mCurrentTasks.containsKey(id)) {
			return mCurrentTasks.get(id);
		}
		MusesLog.D(d, TAG, "add task name = " + name + "  taskid = " + id);
		if (getDownSave(dir) == null || getDownSave(dir).isEmpty()) {
			mListener.onError(null, DownloadTaskListener.DOWNLOAD_ERROR_FILE_NOT_FOUND);
			return null;
		}

		DownloadDBEntity dbEntity = mDbHelper.getDownLoadedList(id + "");
		//是否有缓存
		if (dbEntity == null) {
			dbEntity = new DownloadDBEntity(id, 0l,
					0l, url, getDownSave(dir), name, DownloadStatus.DOWNLOAD_STATUS_INIT);
			mDbHelper.insert(dbEntity);
		}
		DownloadTask downloadTask = DownloadTask.parse(dbEntity, mContext);
		if (downloadTask != null && !mCurrentTasks.containsKey(downloadTask.getId())) {
			MusesLog.D(d, TAG, "task id = " + downloadTask.getId());
			mCurrentTasks.put(downloadTask.getId(), downloadTask);
		}
		return downloadTask;
	}

	public void startTask(DownloadTask downloadTask) {
		MusesLog.D(d, TAG, TAG + " start task task size = " + mCurrentTasks.size());
		if (downloadTask == null) {
			MusesLog.D(d, TAG, "can't start downloadtask");
			return;
		}
		MusesLog.D(d, TAG, "start task ,task name = " + downloadTask.getFileName() + "  taskid = " + downloadTask.getId());
		if (downloadTask.getDownloadStatus() != DownloadStatus.DOWNLOAD_STATUS_COMPLETED) {
			downloadTask.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PREPARE);
			downloadTask.setdownFileStore(mDbHelper);
			Log.e(TAG, "(mListener == null) " + (mListener == null));
			downloadTask.addDownloadListener(mListener);
			// TODO: 2018/4/11  自定义下载任务优先级
			mExecutorService.execute(new PriorityRunnable(Priority.LOW, downloadTask));
		} else {
			mListener.onCompleted(downloadTask);
		}
	}

	/**
	 * resume task with specific taskId
	 *
	 * @param taskId
	 * @return
	 */
	public void resume(String taskId) {
		DownloadTask resumeTask = mCurrentTasks.get(taskId);
		if (resumeTask == null || resumeTask.getDownloadStatus() == DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING) {
			MusesLog.D(d, TAG, "no resume task = " + taskId);
			return;
		}
		startTask(resumeTask);
		MusesLog.D(d, TAG, "resume task = " + taskId);
	}

	/**
	 * cancel task with specific taskId
	 *
	 * @param taskId
	 */
	public void cancel(String taskId) {
		mDbHelper.deleteTask(taskId);
		DownloadTask cancelTask = mCurrentTasks.get(taskId);
		if (cancelTask == null) {
			MusesLog.D(d, TAG, "no cancel task = " + taskId);
			return;
		}
		cancelTask.cancel();
		mCurrentTasks.remove(taskId);
		MusesLog.D(d, TAG, "cancel task = " + taskId);
	}

	/**
	 * pause task with specific taskId
	 *
	 * @param taskId
	 */
	public void pause(String taskId) {
		DownloadTask pauseTask = mCurrentTasks.get(taskId);
		if (pauseTask == null) {
			MusesLog.D(d, TAG, "no pause task = " + taskId);
			return;
		}
		pauseTask.pause();

		MusesLog.D(d, TAG, "pause task = " + taskId);
	}
}
