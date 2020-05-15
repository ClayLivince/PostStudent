package xyz.cyanclay.buptallinone.network;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

import xyz.cyanclay.buptallinone.R;

public class UpdateManager {

    private NetworkManager nm;
    private DownloadManager downloadManager;
    private JSONObject json;
    private long id = 0;

    private static final String versionURL = "https://www.cyanclay.xyz/bupt/version.json";
    private static final String apkURL = "https://www.cyanclay.xyz/bupt/latest.apk";

    UpdateManager(NetworkManager nm) {
        this.nm = nm;
        downloadManager = (DownloadManager) nm.context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void update() {
        Uri uri = Uri.parse(apkURL);
        System.out.println(uri.getHost());

        nm.context.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setDestinationInExternalFilesDir(nm.context, "/update/", "app-latest.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("Download");
        request.setDescription("Downloading Latest BUPT-APP");
        request.setMimeType("application/vnd.android.package-archive");
        request.allowScanningByMediaScanner();
        request.setVisibleInDownloadsUi(true);
        id = downloadManager.enqueue(request);

        DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
        try (Cursor cursor = downloadManager.query(query)) {
            if (cursor.moveToFirst()) {
                int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int downloadStatus = cursor.getInt(statusColumn);
                if (DownloadManager.STATUS_PAUSED == downloadStatus) {
                    int reasonColumn = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int reasonCode = cursor.getInt(reasonColumn);
                    Log.e("TAG", "Download paused: " + reasonCode);
                } else if (DownloadManager.STATUS_SUCCESSFUL == downloadStatus
                        || DownloadManager.STATUS_FAILED == downloadStatus) {
                    Log.i("TAG", "Download Ended");
                }
            }
        }

    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(nm.context)
                            .setSmallIcon(R.drawable.xiaohui)
                            .setContentTitle("Kopi81")
                            .setContentText("Download completed !");
            NotificationManager notificationManager = (NotificationManager) nm.context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(455, mBuilder.build());

        }
    };


    public boolean checkForUpdates() throws IOException, JSONException {
        Connection.Response res = nm.getNoVPN(Jsoup.connect(versionURL)
                .ignoreContentType(true));
        json = new JSONObject(res.body());
        String version = json.getString("version");
        return compare(version);
    }

    /**
     * @return infos
     * 0: versionName
     * 1: infoTitle
     * 2: infoDetail
     */
    public String[] getUpdateInfo() throws IOException, JSONException {
        String[] info = new String[3];

        if (json == null) checkForUpdates();
        info[0] = json.getString("version");
        info[1] = json.getString("title");
        info[2] = json.getString("content");

        return info;
    }

    private boolean compare(String version) {
        PackageManager packageManager = nm.context.getPackageManager();

        try {
            PackageInfo packInfo = packageManager.getPackageInfo(nm.context.getPackageName(), 0);
            String currentVersion = packInfo.versionName;
            return version.compareTo(currentVersion) > 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}