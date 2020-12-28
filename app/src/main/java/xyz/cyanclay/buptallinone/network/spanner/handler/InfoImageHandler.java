package xyz.cyanclay.buptallinone.network.spanner.handler;

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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.info.InfoManager;
import xyz.cyanclay.buptallinone.network.login.LoginException;

public class InfoImageHandler extends ImageHandler {

    private int maxWidth;
    private boolean forceRefresh;
    private InfoManager infoManager;

    private static Logger logger = LogManager.getLogger(NetworkManager.class);

    public InfoImageHandler(InfoManager mgr) {
        this.infoManager = mgr;
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
        Bitmap bitmap = null;
        try {
            URL packed = new URL(url);
            bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(
                    infoManager.getBytes(url, forceRefresh)));
        } catch (IOException | LoginException e) {
            if (e instanceof MalformedURLException) {
                try {
                    String[] pieces = url.split("://");
                    return getBitmap("http://" + pieces[1]);
                } catch (ArrayIndexOutOfBoundsException outBound) {
                    logger.error("Malformed URL:" + url, e);
                }
            } else e.printStackTrace();
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
