package com.sendi.deliveredrobot.navigationtask;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadBill {
    private static DownloadBill instance;
    private List<DownloadTask> taskList;
    private DownloadTask currentTask;

    private DownloadBill() {
        taskList = new ArrayList<>();
    }

    public static DownloadBill getInstance() {
        if (instance == null) {
            instance = new DownloadBill();
        }
        return instance;
    }
/**
 * @param url 下载网络地址
 * @param path 想要存储的路径
 * @param fileName 自定义存储的名字
 * @param listener 监听
 */
    public void addTask(String url, String path, String fileName, DownloadListener listener) {
        DownloadTask task = new DownloadTask(url, path, fileName, listener);
        taskList.add(task);
        if (currentTask == null) {
            startNextTask();
        }
    }


    private void startNextTask() {
        if (taskList.size() > 0) {
            currentTask = taskList.remove(0);
            currentTask.execute();
        } else {
            currentTask = null;
        }
    }

    /*
     *任务下载完整
     */
    public boolean isAllTasksFinished() {
        return taskList.isEmpty() && currentTask == null;
    }

    public interface DownloadListener {
        void onProgress(int progress);

        void onFinish();

        void onError(Exception e);
    }

    public int getTaskCount() {
        return taskList.size();
    }

    private class DownloadTask extends AsyncTask<Void, Integer, Boolean> {
        private String url;
        private String directory;
        private String fileName;
        private DownloadListener listener;

        public DownloadTask(String url, String directory, String fileName, DownloadListener listener) {
            this.url = url;
            this.directory = directory;
            this.fileName = fileName;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                URL url = new URL(this.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int totalLength = conn.getContentLength();
                is = conn.getInputStream();
                File directoryFile = new File(directory);
                if (!directoryFile.exists()) {
                    directoryFile.mkdirs();
                }
                File file = new File(directoryFile, fileName);
                fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                int downloadedLength = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    downloadedLength += len;
                    int progress = (int) ((downloadedLength / (float) totalLength) * 100);
                    publishProgress(progress);
                }
                fos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (listener != null) {
                listener.onProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (listener != null) {
                if (result) {
                    listener.onFinish();
                } else {
                    listener.onError(new Exception("Download failed"));
                }
            }
            startNextTask();
        }
    }
}
