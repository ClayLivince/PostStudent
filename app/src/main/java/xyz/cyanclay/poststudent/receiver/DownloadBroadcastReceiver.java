package xyz.cyanclay.poststudent.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import xyz.cyanclay.poststudent.R;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

    String fileName = null;

    /**
     * Required empty constructor.
     */
    public DownloadBroadcastReceiver() {
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getResources().getString(R.string.file_name_downloaded, fileName),
                Toast.LENGTH_SHORT).show();
    }
}
