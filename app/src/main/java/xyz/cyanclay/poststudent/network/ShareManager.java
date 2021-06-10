package xyz.cyanclay.poststudent.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.info.InfoCategory;
import xyz.cyanclay.poststudent.network.info.InfoManager.InfoItem;

public class ShareManager {

    private Context context;

    ShareManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void share(InfoItem item, Activity activity) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_TEXT, item.titleFull
                + "\n" + buildShareURL(item)
                + "\n分享自北邮一点通APP"
        );
        shareIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_to));
        activity.startActivity(shareIntent);
    }

    private String buildShareURL(InfoItem item) {
        return "https://webapp.bupt.edu.cn/extensions/wap/news/detail.html?id="
                + item.id
                + "&classify_id=" + getClassifyID(item.category);
    }

    private String getClassifyID(InfoCategory category) {
        if (InfoCategory.getRootCategory().getSubCategory(0).id.equals(category.id)) {
            return "tzgg";
        } else if (InfoCategory.getRootCategory().getSubCategory(1).id.equals(category.id)) {
            return "xnxw";
        }
        return "";
    }

}
