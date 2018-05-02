package com.green.kinsomy.downloader;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.green.kinsomy.downloader.db.DbHelper;
import com.green.kinsomy.downloader.db.DownloadDBEntity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 下载任务类
 * Created by kinsomy on 2018/4/9.
 */

public class DownloadTask implements Runnable, Parcelable {

	private DownloadDBEntity mDbEntity;
	private DbHelper mDbHelper;

	private String mId;
	private long mTotalSize;
	private long mCompletedSize;         //  Download section has been completed
	private String mUrl;
	private String mSaveDirPath;
	private int mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_INIT;
	private long PERIOD = 2000;
	private final int MAX_RETRY = 3;
	private int mRetry = 0;

	private String mFileName;    // File name when saving
	private String TAG = "DownloadTask";
	private DownloadTaskListener mListener;

	private TimerTask mTimerTask;
	private Timer mTimer;

	protected DownloadTask(Context context, Builder builder) {
		mDbHelper = DbHelper.getInstance(context);
		init(builder);
	}

	protected DownloadTask(Parcel in) {
		mId = in.readString();
		mTotalSize = in.readLong();
		mCompletedSize = in.readLong();
		mUrl = in.readString();
		mSaveDirPath = in.readString();
		mDownloadStatus = in.readInt();
		mFileName = in.readString();
	}

	private void init(Builder builder) {
		mFileName = builder.fileName;
		mSaveDirPath = builder.saveDirPath;
		mCompletedSize = builder.completedSize;
		mDbEntity = builder.dbEntity;
		mUrl = builder.url;
		mTotalSize = builder.totalSize;
		mCompletedSize = builder.completedSize;
		mId = builder.id;
		mDownloadStatus = builder.downloadStatus;
	}

	@Override
	public void run() {
		mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_PREPARE;
		onPrepare();
		Log.d(TAG, "run: onPrepare()");
		RandomAccessFile randomAccessFile = null;
		InputStream inputStream = null;
		BufferedInputStream bis = null;
		HttpURLConnection conn;
		try {
			mDbEntity = mDbHelper.getDownLoadedList(mId);
			randomAccessFile = new RandomAccessFile(mSaveDirPath + mFileName, "rwd");
			mCompletedSize = randomAccessFile.length();//获取现在已下载的文件长度
			mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_START;
			onStart();
			Log.d(TAG, "run: onStart()");

			URL url = new URL(mUrl);
			conn = Connect(url, true);
			if (conn != null) {
				inputStream = conn.getInputStream();
				if (mTotalSize <= 0) {
					mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING_WITHOUT_PROGRESS;
				} else {
					mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING;
				}

				startTimer();
				bis = new BufferedInputStream(inputStream);
				byte[] buffer = new byte[4 * 1024];
				int length = 0;
				if (mDbEntity == null) {
					mDbEntity = new DownloadDBEntity(mId, mTotalSize, mCompletedSize, mUrl, mSaveDirPath, mFileName, mDownloadStatus);
					mDbHelper.insert(mDbEntity);
				} else {
					mDbEntity.setTotalSize(mTotalSize);
					mDbEntity.setDownloadStatus(mDownloadStatus);
					mDbEntity.setCompletedSize(mCompletedSize);
					mDbHelper.update(mDbEntity);
				}
				randomAccessFile.seek(mCompletedSize);
				while ((length = bis.read(buffer)) > 0 && mDownloadStatus != DownloadStatus.DOWNLOAD_STATUS_CANCEL && mDownloadStatus != DownloadStatus.DOWNLOAD_STATUS_PAUSE) {
					randomAccessFile.write(buffer, 0, length);
					mCompletedSize += length;
				}

				mCompletedSize = randomAccessFile.length();
				Log.d(TAG, "run: after while " + mCompletedSize + "---" + mTotalSize);
				if ((mCompletedSize == mTotalSize && mDownloadStatus == DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING)
						|| mDownloadStatus == DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING_WITHOUT_PROGRESS) {
					mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_COMPLETED;
				}
				onDownloading();
			}
		} catch (FileNotFoundException e) {
			mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
			onError(DownloadTaskListener.DOWNLOAD_ERROR_FILE_NOT_FOUND);
			return;
		} catch (IOException e) {
			mDownloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
			onError(DownloadTaskListener.DOWNLOAD_ERROR_IO_ERROR);
			return;
		} finally {
			stopTimer();
			mDbEntity.setCompletedSize(mCompletedSize);
			mDbEntity.setDownloadStatus(mDownloadStatus);
			mDbHelper.update(mDbEntity);
			if (bis != null) try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (inputStream != null) try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (randomAccessFile != null) try {
				randomAccessFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		handleStatus(mDownloadStatus);
	}

	private void handleStatus(int status) {
		switch (status) {
			case DownloadStatus.DOWNLOAD_STATUS_COMPLETED:
				onCompleted();
				break;
			case DownloadStatus.DOWNLOAD_STATUS_PAUSE:
				onPause();
				break;
			case DownloadStatus.DOWNLOAD_STATUS_CANCEL:
				mDbHelper.deleteTask(mDbEntity.getDownloadId());
				File temp = new File(mSaveDirPath + mFileName);
				if (temp.exists()) temp.delete();
				onCancel();
				break;
		}
	}

	//连接
	private HttpURLConnection Connect(URL url, boolean supportBP) throws IOException {
		HttpURLConnection conn;
		conn = (HttpURLConnection) url.openConnection();
		conn = HttpConnectionHelper.setConnectParam(conn);
		if (supportBP) {
			conn.setRequestProperty("Range", "bytes=" + mCompletedSize + "-");
		}
		conn.connect();
		int code = conn.getResponseCode();
		long len = conn.getContentLength();
		Log.d(TAG, mFileName + "Connect: code" + code);
		if (code == HttpURLConnection.HTTP_PARTIAL) {
			if (len > 0 && mTotalSize <= 0) {
				mTotalSize = len;
				Log.d(TAG, mFileName + "run : content-length" + mTotalSize);
			}
			//支持断点，直接下载
			return conn;
		} else if (code == HttpURLConnection.HTTP_OK) {
			if (len > 0 && mTotalSize <= 0) {
				mTotalSize = len;
				Log.d(TAG, mFileName + "run : content-length" + mTotalSize);
			}
			//不支持断点，清空已完成进度
			mCompletedSize = 0;
			return Connect(url, false);
		} else if (mRetry <= MAX_RETRY) {
			Log.d(TAG, "run: mRetry" + mFileName + "--" + mRetry);
			mRetry++;
			return Connect(url, true);
		} else {
			mRetry = 0;
			onError(DownloadTaskListener.DOWNLOAD_ERROR_HTTP_ERROR);
			return null;
		}
	}

	private void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					onDownloading();
				}
			};
		}

		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, PERIOD, PERIOD);
		}
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeLong(mTotalSize);
		dest.writeLong(mCompletedSize);
		dest.writeString(mUrl);
		dest.writeString(mSaveDirPath);
		dest.writeInt(mDownloadStatus);
		dest.writeString(mFileName);
	}

	public static final Creator<DownloadTask> CREATOR = new Creator<DownloadTask>() {
		@Override
		public DownloadTask createFromParcel(Parcel in) {
			return new DownloadTask(in);
		}

		@Override
		public DownloadTask[] newArray(int size) {
			return new DownloadTask[size];
		}
	};

	public static class Builder {

		private String url;
		private String fileName = url;    // File name when saving
		private String saveDirPath;
		private Context context;
		private DownloadDBEntity dbEntity = null;

		private String id;
		private long totalSize;
		private long completedSize;         //  Download section has been completed

		private int downloadStatus = DownloadStatus.DOWNLOAD_STATUS_INIT;

		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}

		public Builder setDBEntity(DownloadDBEntity dbEntity) {
			this.dbEntity = dbEntity;
			this.downloadStatus = dbEntity.getDownloadStatus();
			this.url = dbEntity.getUrl();
			this.id = dbEntity.getDownloadId();
			this.fileName = dbEntity.getFileName();
			this.saveDirPath = dbEntity.getSaveDirPath();
			this.completedSize = dbEntity.getCompletedSize();
			this.totalSize = dbEntity.getTotalSize();
			return this;
		}


		public DownloadTask build() {
			return new DownloadTask(context, this);
		}
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public long getTotalSize() {
		return mTotalSize;
	}

	public void setTotalSize(long totalSize) {
		this.mTotalSize = totalSize;
	}

	public long getCompletedSize() {
		return mCompletedSize;
	}

	public void setCompletedSize(long completedSize) {
		this.mCompletedSize = completedSize;
	}

	public int getDownloadStatus() {
		return mDownloadStatus;
	}

	public void setDownloadStatus(int downloadStatus) {
		this.mDownloadStatus = downloadStatus;
	}

	public void setdownFileStore(DbHelper dbHelper) {
		this.mDbHelper = dbHelper;
	}

	public String getFileName() {
		return mFileName;
	}

	public String getSaveDirPath() {
		return mSaveDirPath;
	}

	public void cancel() {
		setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_CANCEL);
		File temp = new File(mSaveDirPath + mFileName);
		if (temp.exists()) temp.delete();
	}

	public void pause() {
		setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PAUSE);

	}

	private void onPrepare() {
		if (mListener == null) {
			return;
		}
		mListener.onPrepare(this);
	}

	private void onStart() {
		if (mListener == null) {
			return;
		}
		mListener.onStart(this);
	}

	private void onDownloading() {
		if (mListener == null) {
			return;
		}
		mListener.onDownloading(this);
	}

	private void onCompleted() {
		if (mListener == null) {
			return;
		}
		mListener.onCompleted(this);
	}

	private void onPause() {
		if (mListener == null) {
			return;
		}

		if (mDbEntity != null) {
			mDbEntity.setCompletedSize(mCompletedSize);
			mDbEntity.setDownloadStatus(mDownloadStatus);
			mDbHelper.update(mDbEntity);
		}
		mListener.onPause(this);

	}

	private void onCancel() {
		if (mListener == null) {
			return;
		}
		mListener.onCancel(this);
	}

	private void onError(int errorCode) {
		if (mListener == null) {
			return;
		}
		mListener.onError(this, errorCode);
	}

	public void addDownloadListener(DownloadTaskListener listener) {
		Log.e(TAG, "(mListener == null) " + (mListener == null));
		if (listener != null)
			mListener = listener;
	}

	public static DownloadTask parse(DownloadDBEntity entity, Context context) {
		DownloadTask task = new Builder(context).setDBEntity(entity).build();
		return task;
	}
}
