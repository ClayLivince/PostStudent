package xyz.cyanclay.poststudent.network.spanner.handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import net.nightwhistler.htmlspanner.handlers.ImageHandler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.network.VPNManager;
import xyz.cyanclay.poststudent.network.info.InfoManager;

public class InfoImageHandler extends ImageHandler {

    private int maxWidth;
    private boolean forceRefresh;
    private InfoManager infoManager;
    private Bitmap failed;

    private static Logger logger = LogManager.getLogger(NetworkManager.class);

    public InfoImageHandler(InfoManager mgr) {
        this.infoManager = mgr;
        failed = BitmapFactory.decodeResource(infoManager.context.getResources(), R.drawable.empty);
    }

    public InfoImageHandler(InfoManager mgr, int maxWidth, boolean force) {
        this.infoManager = mgr;
        this.maxWidth = maxWidth;
        this.forceRefresh = force;
    }

    public void setProps(int maxWidth, boolean forceRefresh) {
        this.maxWidth = maxWidth;
        this.forceRefresh = forceRefresh;
    }

    @Override
    public void handleTagNode(TagNode node, SpannableStringBuilder builder,
                              int start, int end) {
        String src = node.getAttributeByName("src");

        builder.append("\uFFFC");

        Bitmap bitmap = getBitmap(src);

        if (bitmap != null) {
            Drawable drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth() - 1,
                    bitmap.getHeight() - 1);
            builder.setSpan(new ImageSpan(drawable), start, end + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private Bitmap getBitmap(String url) {
        if (url == null)
            return null;
        Bitmap bitmap = null;
        URL packed;
        try {
            packed = new URL(url);
        } catch (MalformedURLException e) {
            if (!infoManager.nm.isSchoolNet) {
                try {
                    packed = new URL(VPNManager.packageURL(url));
                } catch (MalformedURLException ee) {
                    try {
                        String[] pieces = url.split("://");
                        packed = new URL("http://" + pieces[1]);
                    } catch (MalformedURLException eee) {
                        logger.error("Malformed URL:" + url, eee);
                        return failed;
                    }
                }
            } else {
                logger.error("Malformed URL:" + url, e);
                return failed;
            }
        }
        try {
            bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(
                    infoManager.getBytes(packed.toString(), forceRefresh)));
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        if (bitmap != null) {
            if (bitmap.getWidth() > maxWidth) {
                if (maxWidth <= 0) {
                    maxWidth = 1000;
                    Log.e("ImageHandler", "maxWidth <= 0, this is an error.");
                }
                double ratio = (double) maxWidth / (double) bitmap.getWidth();
                double scaledHeight = (double) bitmap.getHeight() * ratio;

                if (scaledHeight <= 0) scaledHeight = 1;
                Matrix matrix = new Matrix();
                matrix.postScale(maxWidth, (int) scaledHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, (int) scaledHeight, false);
            }
        }
        return bitmap;
    }
}
