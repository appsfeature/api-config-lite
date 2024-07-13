package com.configlite.network.download;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.configlite.ConfigManager;
import com.configlite.R;
import com.configlite.network.RetrofitApiInterface;
import com.configlite.type.ApiHost;
import com.configlite.util.NetworkLog;
import com.configlite.util.NetworkUtility;
import com.helper.task.TaskRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DownloadManager {

    private File baseDirectory;
    private final Progress progress;
    private String fileName;
    private String baseUrl;

    /**
     * @param baseDirectory : Folder where save file
     */
    public DownloadManager setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
        return this;
    }

    /**
     * @param fileName : fileName of file to download
     */
    public DownloadManager setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * @param baseUrl : baseUrl of file to download
     */
    public DownloadManager setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public DownloadManager(Context context) {
        this.progress = (Progress) context;
        if (ConfigManager.getInstance() != null) {
            ConfigManager.getInstance().setDownloadListener(new ConfigDownloadListener() {
                @Override
                public void onProgressUpdate(int mProgress) {
                    if(progress != null){
                        progress.onProgressUpdate(mProgress);
                    }
                }
            });
        }
    }

    public void loadFileIfExists(Context context, String fileName) {
        if (fileName == null) {
            progress.onDownloadingError(new Exception("Invalid file name."));
            return;
        }
        this.fileName = fileName;
        startFileDownload(context);
    }


    public void downloadFile(Context context, String fileUrl) {
        if (fileUrl == null) {
            progress.onDownloadingError(new Exception("Invalid file name."));
            return;
        } else if (TextUtils.isEmpty(fileUrl)) {
            progress.onDownloadingError(new Exception("Invalid file url."));
            return;
        }
        if (NetworkUtility.isConnected(context)) {
            downloadFileRetrofit(context, fileUrl);
        } else
            progress.onDownloadingError(new Exception("No internet connection."));
    }

    private boolean isCancelDownload = false;

    public void cancelDownload() {
        isCancelDownload = true;
        if ( executor != null ) {
            executor.shutdownNow();
        }
    }

    public interface Progress {
        void onProgressManager(boolean isVisible);

        void onShowAdsInUi();

        void onDownloadedFileStatus(boolean isFileExists);

        void onProgressUpdate(int progress);

        void onFileDownloaded(File file, Uri fileUri, String ext, String type, Boolean isFileAlreadyDownloaded);

        void onDownloadingError(Exception e);

        void onDownloadingCanceled();
    }

    private void downloadFileRetrofit(Context context, String fileUrl) {
        progress.onProgressManager(true);
        if (ConfigManager.getInstance() != null) {
            Map<String, String> map = new HashMap<>();

            RetrofitApiInterface apiInterface;
            if(!TextUtils.isEmpty(baseUrl)) {
                apiInterface = ConfigManager.getInstance().getHostDownloadInterface(baseUrl);
            }else {
                apiInterface = ConfigManager.getInstance().getApiDownloadInterface(ApiHost.HOST_DOWNLOAD);
            }
            if (apiInterface != null) {
                apiInterface.downloadPDFFileWithDynamicUrlAsync(fileUrl, map).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                        String fileSizeString = response.headers().get("Content-Length-X");
                        long fileSize = -1;
                        if (!TextUtils.isEmpty(fileSizeString)) {
                            try {
                                fileSize = Long.parseLong(fileSizeString);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        if (response.isSuccessful()) {
                            handleResponseBody(context, response.body(), fileSize);
                        } else {
                            progress.onDownloadingError(new Exception("Api Failed"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call,@NonNull Throwable t) {
                        progress.onDownloadingError(new Exception(t.getMessage()));
                    }
                });
            }else {
                progress.onDownloadingError(new Exception("Invalid Interface"));
            }
        }
    }

    private ExecutorService executor ;
    private void handleResponseBody(Context context, ResponseBody body , long fileSize) {
        executor = Executors.newCachedThreadPool();
        Handler handler = TaskRunner.getInstance().getHandler();
        executor.execute(() -> {
            try {
                boolean isCompleteDownload = writeResponseBodyToDisk(context, body);
                handler.post(() -> {
                    onFileDownloadComplete(context, isCompleteDownload ? "success" : "failure");
                });
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    onFileDownloadComplete(context, e.getMessage());
                });
            }
        });
    }

    private void onFileDownloadComplete(Context context, String result){
        if (progress != null) {
            progress.onProgressManager(false);
            if (!isCancelDownload) {
                if (result.equalsIgnoreCase("success")) {
                    downloadFinished(context,false);
                } else {
                    progress.onDownloadingError(new Exception(result));
                }
            } else {
                progress.onDownloadingCanceled();
            }
        }
    }

    private boolean writeResponseBodyToDisk(Context context, ResponseBody body) {
        try {
            File apkStorage = null;
            if (isSDCardPresent()) {
                apkStorage = baseDirectory;
            }
            //If File is not present create directory
            if (apkStorage != null && !apkStorage.exists()) {
                apkStorage.mkdir();
                // Log.e(TAG, "Directory Created.");
            }
            File futureStudioIconFile = new File(apkStorage, fileName);//Create Output file in Main File
            //Create New File if not present
            if (!futureStudioIconFile.exists()) {
                futureStudioIconFile.createNewFile();
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                if (e.getMessage() != null) {
                    Log.e(this.getClass().getSimpleName(), e.getMessage());
                }
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            if (e.getMessage() != null) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }
            return false;
        }
    }


    public static boolean isSDCardPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    private void startFileDownload(Context context) {
        if (checkFileIsCompleteDownloaded(fileName)) {
            progress.onDownloadedFileStatus(true);
            downloadFinished(context,true);
            progress.onProgressManager(false);
        } else {
            progress.onProgressManager(false);
            progress.onShowAdsInUi();
            progress.onDownloadedFileStatus(false);
        }
    }


    private void downloadFinished(Context context, Boolean isFileAlreadyDownloaded) {
        File file = getFile(getValidFile(fileName));

        if (file.exists()) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);

            Uri fileUri = getUriFromFile(context, file);
            progress.onFileDownloaded(file, fileUri, ext, type, isFileAlreadyDownloaded);
        }
//        else {
//            if(TextUtils.isEmpty(PDFSupportPref.getDownloadDirectory(context))){
//                File pdfViewerPath = getFile(new File(getValidFile( "PDFViewer"), fileName));
//                if(pdfViewerPath.exists()) {
//                    MimeTypeMap mime = MimeTypeMap.getSingleton();
//                    String ext = pdfViewerPath.getName().substring(pdfViewerPath.getName().lastIndexOf(".") + 1);
//                    String type = mime.getMimeTypeFromExtension(ext);
//
//                    Uri fileUri = getUriFromFile(pdfViewerPath);
//                    progress.onFileDownloaded(pdfViewerPath, fileUri, ext, type, isFileAlreadyDownloaded);
//                }
//            }
//        }
    }

    public File getValidFile(String fileName) {
        return new File(baseDirectory, fileName);
    }

    private Uri getUriFromFile(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + context.getString(R.string.file_provider), file);
    }

    public static void grantAllUriPermissions(Context context, Intent intent, Uri uri) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    private boolean checkFileIsCompleteDownloaded(String fileName) {
        try {
            List<String> fileList = getStorageFileList();
            if (fileList != null) {
                boolean isContain = fileList.contains(fileName);
                if (!isContain) {
                    String file = fileName.endsWith(".pdf")
                            ? fileName.replace(".pdf", "")
                            : fileName + ".pdf";
                    return fileList.contains(file);
                } else
                    return isContain;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getStorageFileList() {
        List<String> result = new ArrayList<>();
        try {
            File path = baseDirectory;
            String[] storageFileList = path.list();
            if (storageFileList != null) {
                result.addAll(Arrays.asList(storageFileList));
            }
            NetworkLog.log("getFileStoreDirectory : " + path.getAbsolutePath());

//            if (TextUtils.isEmpty(PDFSupportPref.getDownloadDirectory(context))) {
//                File pdfViewerPath = getValidFile( "PDFViewer");
//                if (pdfViewerPath.exists()) {
//                    String[] pdfViewerFileList = pdfViewerPath.list();
//                    if (pdfViewerFileList != null) {
//                        result.addAll(Arrays.asList(pdfViewerFileList));
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    private File getFile(File file) {
        String filePath = file.getAbsolutePath();
        if (file.exists()) {
            return file;
        } else {
            String filePathNew = filePath.endsWith(".pdf")
                    ? filePath.replace(".pdf", "")
                    : filePath + ".pdf";
            file = new File(filePathNew);
        }
        return file;
    }

}
