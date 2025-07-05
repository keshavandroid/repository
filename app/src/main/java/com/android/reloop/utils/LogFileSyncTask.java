package com.android.reloop.utils;


import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.reloop.reloop.app.MainApplication;
import com.reloop.reloop.network.Network;
import com.reloop.reloop.network.NetworkCall;
import com.reloop.reloop.network.serializer.BaseResponse;
import com.reloop.reloop.utils.RequestCodes;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class LogFileSyncTask {
    private static final String TAG = LogFileSyncTask.class.getSimpleName();

    private Context context;
    private AtomicBoolean isProcessRunning = new AtomicBoolean(false);

    public LogFileSyncTask(Context context) {
        setContext(context);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 888) {
                isProcessRunning.set(false);
            }
        }
    };

    private static LogFileSyncTask task;

    public static LogFileSyncTask getInstance(Context context) {
        if (task == null) {
            task = new LogFileSyncTask(context);
        } else {
            task.setContext(context);
        }
        return task;
    }

    public void executeTask() {
        executor.shutdownNow();
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            isProcessRunning.set(true);
            startServerSync();
        });
    }

    private void startServerSync() {
        Log.d(TAG, "Logs Lookup called");
        try {
            //File logFileCopy = lockFile(user.user_id);
            List<LogFile> logFiles = DatabaseHelper.getInstance(MainApplication.Companion.applicationContext()).getLogFiles();
            for (LogFile file : logFiles) {
                try {
                    if (sendFile(file)) {
                        file.setIsUploaded("1");
                        DatabaseHelper.getInstance(MainApplication.Companion.applicationContext()).updateLogFile(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LogManager", "LM Failed To Upload Logs To Server...");
        }

        handler.sendEmptyMessage(888);
    }
    private boolean sendFile(LogFile logFile) throws Exception {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version = Build.VERSION.SDK_INT + "";
        String deviceInfo = manufacturer.toUpperCase() + "_" + model;

        File file = new File(logFile.filePath);

        MultipartBody.Part bodyPart = MultipartBody.Part.createFormData(
                "log_file",
                file.getName(),
                RequestBody.create(MediaType.parse("text/plain"), file)
        );

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(bodyPart)
                .addFormDataPart("device_name", deviceInfo)
                .addFormDataPart("device_os", version)
                .build();

        if (file.exists()) {
            try {
                Response<Object> response = NetworkCall.INSTANCE.make()
                        .setCallback(null)
                        .setTag(RequestCodes.API.LOG_FILE_UPLOAD)
                        .autoLoading(null)
                        .enque(new Network().apis().pushLogFile(requestBody))
                        .executeCurrentThread();
                return BaseResponse.Companion.isSuccess(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
