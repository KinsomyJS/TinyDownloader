package com.green.kinsomy.downloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.green.kinsomy.downloader.db.DbHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by kinsomy on 2018/4/9.
 */
public class DownloadService extends Service {
	public static final String ADD_DOWNTASK = "com.green.kinsomy.downloader.downtaskadd";
	public static final String ADD_MULTI_DOWNTASK = "com.green.kinsomy.downloader.multidowntaskadd";
	public static final String CANCLE_DOWNTASK = "com.green.kinsomy.downloader.cacletask";
	public static final String CANCLE_ALL_DOWNTASK = "com.green.kinsomy.downloader.caclealltask";
	public static final String START_ALL_DOWNTASK = "com.green.kinsomy.downloader.startalltask";
	public static final String RESUME_START_DOWNTASK = "com.green.kinsomy.downloader.resumestarttask";
	public static final String PAUSE_TASK = "com.green.kinsomy.downloader.pausetask";
	public static final String PAUSE_ALLTASK = "com.green.kinsomy.downloader.pausealltask";

	public static final String PACKAGE = "com.green.kinsomy.muses";

	private DownloadManager mManager;

	private boolean d = true;
	private static final String TAG = "DownService";
	private static DbHelper dbHelper;
	private static ArrayList<String> prepareTaskList = new ArrayList<>();
	private Context mContext;

	public static ArrayList<String> getPrepareTasks() {
		return prepareTaskList;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MusesLog.D(d, TAG, TAG + " oncreate");
		mContext = this;
		mManager = new DownloadManager(this);
		dbHelper = DbHelper.getInstance(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MusesLog.D(d, TAG, TAG + " onstartcommand");

		String action = null;
		try {
			action = intent.getAction();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (action == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		switch (action) {
			case ADD_DOWNTASK:
				String name = intent.getStringExtra("name");
				String url = intent.getStringExtra("url");
				addDownloadTask(name, url);
				break;
			case ADD_MULTI_DOWNTASK:
				String[] names = intent.getStringArrayExtra("names");
				ArrayList<String> urls = intent.getStringArrayListExtra("urls");
				addMultiDownloadTask(names, urls);
				break;
			case RESUME_START_DOWNTASK:
				String taskid = intent.getStringExtra("downloadid");
				MusesLog.D(d, TAG, "resume task = " + taskid);
				resume(taskid);
				break;
			case PAUSE_TASK:
				String pauseId = intent.getStringExtra("downloadid");
				MusesLog.D(d, TAG, "pause task = " + pauseId);
				pause(pauseId);
				break;
			case CANCLE_DOWNTASK:
				String cancelId = intent.getStringExtra("downloadid");
				MusesLog.D(d, TAG, "cancle task = " + cancelId);
				cancel(cancelId);
				break;
			case START_ALL_DOWNTASK:
				String[] ids = dbHelper.getDownLoadedListAllDowningIds();
				for (int i = 0; i < ids.length; i++) {
					if (!prepareTaskList.contains(ids[i])) {
						prepareTaskList.add(ids[i]);
					}
				}
				break;
			case PAUSE_ALLTASK:
				for (String id : prepareTaskList) {
					pause(id);
				}
				break;
		}
		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	private String getDownSave() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newdownload/");
			if (!file.exists()) {
				boolean r = file.mkdirs();
				if (!r) {
					Toast.makeText(mContext, "储存卡无法创建文件", Toast.LENGTH_SHORT).show();
					return null;
				}
				return file.getAbsolutePath() + "/";
			}
			return file.getAbsolutePath() + "/";
		} else {
			Toast.makeText(mContext, "没有储存卡", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	private void addMultiDownloadTask(String[] names, ArrayList<String> urls) {

		MusesLog.D(d, TAG, "add task name = " + names + "  taskid = " + (urls).hashCode());
		int len = urls.size();
		for (int i = 0; i < len; i++) {
			addDownloadTask(names[i],urls.get(i));
		}
		Toast.makeText(mContext, "已加入到下载", Toast.LENGTH_SHORT).show();

	}

	private void addDownloadTask(String name, String url) {
		DownloadTask task = mManager.addDownloadTask(getDownSave(),name,url,"");
		startTask(task);
	}

	public void startTask(DownloadTask downloadTask) {
		mManager.startTask(downloadTask);
	}

	/**
	 * if return null,the task does not exist
	 *
	 * @param taskId
	 * @return
	 */
	public void resume(String taskId) {
		mManager.resume(taskId);
	}

	/**
	 * cancel task with specific taskId
	 *
	 * @param taskId
	 */
	public void cancel(String taskId) {
		mManager.cancel(taskId);
	}

	/**
	 * pause task with specific taskId
	 *
	 * @param taskId
	 */
	public void pause(String taskId) {
		mManager.pause(taskId);
	}
}
