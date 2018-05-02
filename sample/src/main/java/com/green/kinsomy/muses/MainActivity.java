package com.green.kinsomy.muses;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.green.kinsomy.downloader.AbsDownloadReceiver;
import com.green.kinsomy.downloader.DownloadManager;
import com.green.kinsomy.downloader.DownloadTask;
import com.green.kinsomy.downloader.MusesLog;
import com.green.kinsomy.downloader.db.DbHelper;

import java.util.ArrayList;

/**
 * Created by kinsomy on 2018/4/4.
 */

public class MainActivity extends Activity {

	private static final String TAG = "NewDownloader";
	private RecyclerView mRvDownLoad;
	private DownLoadAdapter mAdapter;
	private DownloadReceiver mDownloadReceiver;
	private DbHelper mDbHelper;
	private ArrayList<DownloadTask> mList = new ArrayList<>();
	private ArrayList<String> mIdList = new ArrayList<>();

	private ArrayList<String> mUrls = new ArrayList<>();
	private ArrayList<String> mNames = new ArrayList<>();
	private DownloadManager mManager;

	private boolean d = true;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_downloader);
		initView();
		mDownloadReceiver = new DownloadReceiver();
		mDownloadReceiver.register(this);
		mManager = new DownloadManager(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDownloadReceiver.unRegister(this);
		if (mManager != null) {
			mManager = null;
		}

	}

	private void startDown() {
		mUrls.add("http://kotlinlang.org/docs/kotlin-docs.pdf");
		mUrls.add("https://atom-installer.github.com/v1.13.0/AtomSetup.exe?s=1484074138&ext=.exe");
		mUrls.add("http://gdown.baidu.com/data/wisegame/0904344dee4a2d92/QQ_718.apk");
		mUrls.add("https://az764295.vo.msecnd.net/stable/f88bbf9137d24d36d968ea6b2911786bfe103002/VSCode-darwin-stable.zip");
		mUrls.add("http://static.gaoshouyou.com/d/36/69/2d3699acfa69e9632262442c46516ad8.apk");
		mNames.add("test1");
		mNames.add("test2");
		mNames.add("test3");
		mNames.add("test4");
		mNames.add("test5");
		DownloadTask task1 = mManager.addDownloadTask(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newdownload/","kotlin-docs.pdf", mUrls.get(0),"test1");
		mManager.startTask(task1);

		DownloadTask task2 = mManager.addDownloadTask(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newdownload/","38&ext=.exe", mUrls.get(1),"test2");
		mManager.startTask(task2);

		DownloadTask task3 = mManager.addDownloadTask(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newdownload/","QQ_718.apk", mUrls.get(2),"test3");
		mManager.startTask(task3);

		DownloadTask task4 = mManager.addDownloadTask(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newdownload/","darwin-stable.zip", mUrls.get(3),"test4");
		mManager.startTask(task4);

		DownloadTask task5 = mManager.addDownloadTask(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newdownload/","6516ad8.apk", mUrls.get(4),"test5");
		mManager.startTask(task5);

	}

	private void initView() {
		mRvDownLoad = (RecyclerView) findViewById(R.id.rv_download);
		mRvDownLoad.setLayoutManager(new LinearLayoutManager(this));
		mAdapter = new DownLoadAdapter(mList, mIdList);
		mRvDownLoad.setAdapter(mAdapter);
		reload();
		((SimpleItemAnimator) mRvDownLoad.getItemAnimator()).setSupportsChangeAnimations(false);
		findViewById(R.id.btn_down).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startDown();
			}
		});

		findViewById(R.id.btn_singletask).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SingleTaskActivity.class));
			}
		});
	}

	private class DownloadReceiver extends AbsDownloadReceiver {



		public void onTaskDownloadingEvent(DownloadTask task, boolean showProgress) {
			if (showProgress) {
				String id = task.getId();
				long complete = task.getCompletedSize();
				long total = task.getTotalSize();
				mAdapter.notifyItem(id, complete, total);
			}
		}

			@Override
		public void onTaskStartEvent(DownloadTask task) {
			if (!mIdList.contains(task.getId())) {
				mList.add(task);
				mIdList.add(task.getId());
				mAdapter.update(mList, mIdList);
			}
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

	private void reload() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				mDbHelper = DbHelper.getInstance(MainActivity.this);
				mList = mDbHelper.getAllRecordTasks();
				for (DownloadTask task : mList) {
					if (!mIdList.contains(task.getId())) {
						mIdList.add(task.getId());
					}
				}
				MusesLog.D(d, TAG, " mlist size = " + mList.size());
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				mAdapter.update(mList, mIdList);
			}
		}.execute();
	}

	class DownLoadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private ArrayList<DownloadTask> mList;
		private ArrayList<String> currentTaskList;


		public DownLoadAdapter(ArrayList<DownloadTask> list, ArrayList<String> currentTaskList) {
			mList = list;
			this.currentTaskList = currentTaskList;
		}

		public void update(ArrayList<DownloadTask> list, ArrayList<String> currentTaskList) {
			this.mList = list;
			this.currentTaskList = currentTaskList;
			notifyDataSetChanged();
		}

		public void notifyItem(String taskId, long completed, long total) {
			int index = currentTaskList.indexOf(taskId);
			if (index != -1) {
				MusesLog.D(d, TAG, "notifyItem " + index + "completed = " + completed + "  total = " + total);
				mList.get(index).setCompletedSize(completed);
				mList.get(index).setTotalSize(total);
				notifyItemChanged(index);
			}
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false));
		}

		@Override
		public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
			final DownloadTask task = mList.get(position);
			if (task.getTotalSize() != 0) {
				int percent = (int) ((100 * task.getCompletedSize()) / task.getTotalSize());
				((ItemViewHolder) holder).mProgressBar.setProgress(percent);
				((ItemViewHolder) holder).mTvProgress.setText(percent + "%");
				((ItemViewHolder) holder).mTvName.setText(task.getFileName());
			}

			((ItemViewHolder) holder).mBtnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (task.getId() != null) {
						mManager.cancel(currentTaskList.get(position));
						mList.remove(position);
						mIdList.remove(position);
						update(mList, mIdList);
					} else {
						Toast.makeText(MainActivity.this, "任务不存在", Toast.LENGTH_SHORT).show();
					}
				}
			});

			((ItemViewHolder) holder).mBtnPause.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (task.getId() != null) {
						mManager.pause(currentTaskList.get(position));
					} else {
						Toast.makeText(MainActivity.this, "任务不存在", Toast.LENGTH_SHORT).show();
					}
				}
			});

			((ItemViewHolder) holder).mBtnResume.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (task.getId() != null) {
						mManager.resume(currentTaskList.get(position));
					} else {
						Toast.makeText(MainActivity.this, "任务不存在", Toast.LENGTH_SHORT).show();
					}
				}
			});

			((ItemViewHolder) holder).mBtnStart.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
		}

		@Override
		public int getItemCount() {
			return mList == null ? 0 : mList.size();
		}

		class ItemViewHolder extends RecyclerView.ViewHolder {

			private Button mBtnStart;
			private Button mBtnCancel;
			private Button mBtnPause;
			private Button mBtnResume;
			private ProgressBar mProgressBar;
			private TextView mTvProgress;
			private TextView mTvName;

			public ItemViewHolder(View itemView) {
				super(itemView);
				mBtnStart = (Button) itemView.findViewById(R.id.btn_start);
				mBtnPause = (Button) itemView.findViewById(R.id.btn_pause);
				mBtnCancel = (Button) itemView.findViewById(R.id.btn_cancel);
				mBtnResume = (Button) itemView.findViewById(R.id.btn_resume);
				mProgressBar = (ProgressBar) itemView.findViewById(R.id.download_progress);
				mTvProgress = (TextView) itemView.findViewById(R.id.download_progress_text);
				mTvName = (TextView) itemView.findViewById(R.id.tv_name);
			}
		}
	}

}
