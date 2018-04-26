package com.green.kinsomy.muses;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.green.kinsomy.downloader.AbsDownloadReceiver;
import com.green.kinsomy.downloader.DownloadManager;
import com.green.kinsomy.downloader.DownloadTask;
import com.green.kinsomy.downloader.MusesLog;


/**
 * Created by kinsomy on 2018/4/18.
 */

public class SingleTaskActivity extends Activity {

	private static final String TAG = "SingleTaskActivity";
	private ProgressBar mSingleProgress;
	private TextView mTvSingle;
	private String mFileName = "kotlin-docs.pdf";
	private String mUrl = "http://kotlinlang.org/docs/kotlin-docs.pdf";
	private boolean d = true;
	private DownStatus downStatus;
	private DownloadManager mManager;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_task);
		mSingleProgress = (ProgressBar) findViewById(R.id.single_progress);
		mTvSingle = (TextView) findViewById(R.id.single_progress_text);

		downStatus = new DownStatus();
		mManager = new DownloadManager(this);
		IntentFilter f = new IntentFilter();
		f.addAction(AbsDownloadReceiver.TASK_STARTDOWN);
		f.addAction(AbsDownloadReceiver.TASK_DOWNLOADING);
		registerReceiver(downStatus, new IntentFilter(f));
		findViewById(R.id.btn_singletask).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				start();
			}
		});
	}

	private void start() {
		DownloadTask task =mManager.addDownloadTask(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newdownload/","kotlin-docs.pdf",mUrl,"id_test");
		mManager.startTask(task);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(downStatus);
	}

	private class DownStatus extends AbsDownloadReceiver {
		@Override
		public void onTaskDownloadingEvent(DownloadTask task) {
			String id = task.getId();
			if(id.equals("id_test")) {
				long complete = task.getCompletedSize();
				long total = task.getTotalSize();
				int percent = (int) ((100 * complete) / total);
				mSingleProgress.setProgress(percent);
				mTvSingle.setText(percent + "%");
			}
		}

		@Override
		public void onTaskStartEvent(DownloadTask task) {
			MusesLog.D(true, TAG, "TASK_STARTDOWN");
		}

		@Override
		public void onTaskErrorEvent(DownloadTask task, int code) {

		}

		@Override
		public void onTaskCancelEvent(DownloadTask task) {

		}

		@Override
		public void onTaskPauseEvent(DownloadTask task) {

		}

		@Override
		public void onTaskCompletedEvent(DownloadTask task) {

		}
	}
}
