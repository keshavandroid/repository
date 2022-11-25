package com.android.reloop.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper
{

    private final String CREATE_LOG_FILE_TABLE = "CREATE TABLE IF NOT EXISTS log_files (row_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,file_name TEXT NOT NULL, file_path TEXT NOT NULL,type TEXT NOT NULL,date_time TEXT NOT NULL, connection_status TEXT NOT NULL, is_uploaded TEXT NOT NULL DEFAULT ('0'));";

    private static int DATABASE_VERSION = 1;
    public static String type = "android";
    private static final String DATABASE_NAME = "reloop-log-database.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, int dbversion, String type) {
        super(context, DATABASE_NAME, null, dbversion);
        DATABASE_VERSION = dbversion;
        DatabaseHelper.type = type;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOG_FILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static DatabaseHelper INSTANCE;
    public static DatabaseHelper getInstance(Context context)
    {
        if(INSTANCE==null) INSTANCE = new DatabaseHelper(context);
        return INSTANCE;
    }

    public void insertLogFile(LogFile logFile) {
        SQLiteDatabase database = this.getWritableDatabase();
        long id = database.insert("log_files", null, logFile.toContentValues());
        logFile.setRowId(id);
    }

    public ArrayList<LogFile> getLogFiles() {
        SQLiteDatabase database = this.getReadableDatabase();
        ArrayList<LogFile> logFiles = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM log_files WHERE is_uploaded = ? ORDER BY row_id ASC", new String[]{"0"});
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    logFiles.add(LogFile.fromCursor(cursor));
                    cursor.moveToNext();
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return logFiles;
    }

    public void updateLogFile(LogFile file) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.update("log_files", file.toContentValues(), "row_id = ?", new String[]{file.getRowId() + ""});
    }
}
