package com.android.reloop.utils;

import android.content.ContentValues;
import android.database.Cursor;

public class LogFile {
    Long rowId;
    String fileName;
    String filePath;
    String type= "LogManager";
    String dateTime = TimeHelper.getDateTime();
    String connectionStatus = "success";
    String isUploaded = "0";

    public LogFile() {
    }

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }


    public String getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        this.isUploaded = isUploaded;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        if (rowId != null) {
            contentValues.put("row_id", rowId);
        }
        contentValues.put("file_name", fileName);
        contentValues.put("file_path", filePath);
        contentValues.put("type", type);
        contentValues.put("date_time", dateTime);
        contentValues.put("connection_status", connectionStatus);
        contentValues.put("is_uploaded", isUploaded);
        return contentValues;
    }

    public static LogFile fromCursor(Cursor cursor) {
        LogFile logFile = new LogFile();
        logFile.rowId = cursor.getLong(cursor.getColumnIndexOrThrow("row_id"));
        logFile.fileName = cursor.getString(cursor.getColumnIndexOrThrow("file_name"));
        logFile.filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
        logFile.type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
        logFile.dateTime = cursor.getString(cursor.getColumnIndexOrThrow("date_time"));
        logFile.connectionStatus = cursor.getString(cursor.getColumnIndexOrThrow("connection_status"));
        logFile.isUploaded = cursor.getString(cursor.getColumnIndexOrThrow("is_uploaded"));
        return logFile;
    }
}
