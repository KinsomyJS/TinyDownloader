package com.green.kinsomy.downloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.green.kinsomy.downloader.DownloadStatus;
import com.green.kinsomy.downloader.DownloadTask;

import java.util.ArrayList;

/**
 * 下载记录DB操作类
 * Created by kinsomy on 2018/4/9.
 */
public class DbHelper {

	private volatile static DbHelper sInstance = null;

	private DownloaderSQLiteOpenHelper mDatabase = null;

	private static Context mContext;

	public DbHelper(final Context context) {
		mDatabase = DownloaderSQLiteOpenHelper.getInstance(context);
	}

	public static synchronized DbHelper getInstance(final Context context) {
		synchronized (DbHelper.class){
			if (sInstance == null) {
				sInstance = new DbHelper(context.getApplicationContext());
			}
			mContext = context;
		}
		return sInstance;
	}

	public void onCreate(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DownFileStoreColumns.NAME + " ("
				+ DownFileStoreColumns.ID + " TEXT NOT NULL PRIMARY KEY," + DownFileStoreColumns.TOOL_SIZE + " INT NOT NULL,"
				+ DownFileStoreColumns.FILE_LENGTH + " INT NOT NULL, " + DownFileStoreColumns.URL + " TEXT NOT NULL,"
				+ DownFileStoreColumns.DIR + " TEXT NOT NULL," + DownFileStoreColumns.FILE_NAME + " TEXT NOT NULL,"
				+ DownFileStoreColumns.DOWNSTATUS + " INT NOT NULL);");
	}

	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DownFileStoreColumns.NAME);
		onCreate(db);
	}

	public synchronized void insert(DownloadDBEntity entity) {
		Log.e("dataen", " id = " + entity.getDownloadId());
		final SQLiteDatabase database = mDatabase.getWritableDatabase();
		database.beginTransaction();
		try {
			ContentValues values = new ContentValues(8);
			values.put(DownFileStoreColumns.ID, entity.getDownloadId());
			values.put(DownFileStoreColumns.TOOL_SIZE, entity.getTotalSize());
			values.put(DownFileStoreColumns.FILE_LENGTH, entity.getCompletedSize());
			values.put(DownFileStoreColumns.URL, entity.getUrl());
			values.put(DownFileStoreColumns.DIR, entity.getSaveDirPath());
			values.put(DownFileStoreColumns.FILE_NAME, entity.getFileName());
			values.put(DownFileStoreColumns.DOWNSTATUS, entity.getDownloadStatus());
			database.replace(DownFileStoreColumns.NAME, null, values);
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
	}

	public synchronized void update(DownloadDBEntity entity) {

		final SQLiteDatabase database = mDatabase.getWritableDatabase();
		database.beginTransaction();
		try {
			ContentValues values = new ContentValues(6);
			values.put(DownFileStoreColumns.TOOL_SIZE, entity.getTotalSize());
			values.put(DownFileStoreColumns.FILE_LENGTH, entity.getCompletedSize());
			values.put(DownFileStoreColumns.URL, entity.getUrl());
			values.put(DownFileStoreColumns.DIR, entity.getSaveDirPath());
			values.put(DownFileStoreColumns.FILE_NAME, entity.getFileName());
			values.put(DownFileStoreColumns.DOWNSTATUS, entity.getDownloadStatus());
			database.update(DownFileStoreColumns.NAME, values, DownFileStoreColumns.ID + " = ?",
					new String[]{entity.getDownloadId()});
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
	}

	public void deleteTask(String Id) {
		final SQLiteDatabase database = mDatabase.getWritableDatabase();
		database.delete(DownFileStoreColumns.NAME, DownFileStoreColumns.ID + " = ?", new String[]
				{String.valueOf(Id)});
	}

	public void deleteTask(String[] Id) {
		final SQLiteDatabase database = mDatabase.getWritableDatabase();
		database.delete(DownFileStoreColumns.NAME, DownFileStoreColumns.ID + " = ?", Id);
	}

	public void deleteDowningTasks() {
		ArrayList<String> results = new ArrayList<>();
		final SQLiteDatabase database = mDatabase.getWritableDatabase();
		Cursor cursor = null;
		try {
			cursor = database.query(DownFileStoreColumns.NAME, null,
					null, null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				results.ensureCapacity(cursor.getCount());

				do {
					if (cursor.getInt(6) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
						results.add(cursor.getString(0));

				} while (cursor.moveToNext());
			}
			String[] t = new String[results.size()];
			for (int i = 0; i < results.size(); i++) {
				t[i] = results.get(i);
			}
			final StringBuilder selection = new StringBuilder();
			selection.append(DownFileStoreColumns.ID + " IN (");
			for (int i = 0; i < t.length; i++) {
				selection.append(t[i]);
				if (i < t.length - 1) {
					selection.append(",");
				}
			}
			selection.append(")");
			database.delete(DownFileStoreColumns.NAME, selection.toString(), null);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}


	}

	public synchronized void deleteAll() {
		final SQLiteDatabase database = mDatabase.getWritableDatabase();
		database.delete(DownFileStoreColumns.NAME, null, null);
	}

	public synchronized DownloadDBEntity getDownLoadedList(String Id) {
		Cursor cursor = null;
		DownloadDBEntity entity = null;
		try {
			cursor = mDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
					DownFileStoreColumns.ID + " = ?", new String[]{String.valueOf(Id)}, null, null, null);
			if (cursor == null) {
				return null;
			}

			if (cursor.moveToFirst()) {

				do {
					entity = new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
							cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6));
				} while (cursor.moveToNext());
				return entity;
			} else return null;

		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	public synchronized ArrayList<DownloadDBEntity> getAllRecord() {
		ArrayList<DownloadDBEntity> results = new ArrayList<>();

		Cursor cursor = null;
		try {
			cursor = mDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
					null, null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				results.ensureCapacity(cursor.getCount());

				do {
					results.add(new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
							cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6)));
				} while (cursor.moveToNext());
			}

			return results;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	public synchronized ArrayList<DownloadTask> getAllRecordTasks() {
		ArrayList<DownloadTask> results = new ArrayList<>();

		Cursor cursor = null;
		try {
			cursor = mDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
					null, null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				results.ensureCapacity(cursor.getCount());

				do {
					results.add(DownloadTask.parse(new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
							cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6)),mContext));
				} while (cursor.moveToNext());
			}

			return results;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	public synchronized ArrayList<DownloadDBEntity> getDownLoadedListAllDowning() {
		ArrayList<DownloadDBEntity> results = new ArrayList<>();

		Cursor cursor = null;
		try {
			cursor = mDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
					null, null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				results.ensureCapacity(cursor.getCount());

				do {
					if (cursor.getInt(6) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
						results.add(new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
								cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6)));
				} while (cursor.moveToNext());
			}

			return results;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	public synchronized String[] getDownLoadedListAllDowningIds() {
		ArrayList<String> results = new ArrayList<>();

		Cursor cursor = null;
		try {
			cursor = mDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
					null, null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				results.ensureCapacity(cursor.getCount());

				do {
					if (cursor.getInt(6) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
						results.add(cursor.getString(0));

				} while (cursor.moveToNext());
			}
			String[] t = new String[results.size()];
			for (int i = 0; i < results.size(); i++) {
				t[i] = results.get(i);
			}
			return t;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	public synchronized ArrayList<DownloadDBEntity> getDownLoadedListAll() {
		ArrayList<DownloadDBEntity> results = new ArrayList<>();

		Cursor cursor = null;
		try {
			cursor = mDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
					null, null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				results.ensureCapacity(cursor.getCount());

				do {

					results.add(new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
							cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6)));
				} while (cursor.moveToNext());
			}

			return results;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}


	public interface DownFileStoreColumns {
		/* Table name */
		String NAME = "downfile_info";

		/* Album IDs column */
		String ID = "id";

		/* Time played column */
		String TOOL_SIZE = "totalsize";

		String FILE_LENGTH = "complete_length";

		String URL = "url";

		String DIR = "dir";
		String FILE_NAME = "file_name";
		String DOWNSTATUS = "notification_type";
	}

}
