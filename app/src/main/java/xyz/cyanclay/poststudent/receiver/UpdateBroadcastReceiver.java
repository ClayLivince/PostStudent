package xyz.cyanclay.poststudent.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

import xyz.cyanclay.poststudent.R;

/**
 * Author : Clay Livince
 * 2021/3/7
 */
public class UpdateBroadcastReceiver extends BroadcastReceiver {

    private String fileName;

    /**
     * Mandatory Empty Constructor Required by Android.
     */
    public UpdateBroadcastReceiver() {

    }

    public UpdateBroadcastReceiver(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, R.string.update_downloaded, Toast.LENGTH_SHORT).show();

        File file = new File(context.getExternalFilesDir("update"), fileName);
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(i);
    }
    /*

    private void openFile(File file, Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.intent.action.VIEW");
        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "没有找到打开此类文件的程序", Toast.LENGTH_SHORT).show();
        }

    }

    private String getMIMEType(File var0) {
        String var1 = "";
        String var2 = var0.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }
     */
}
