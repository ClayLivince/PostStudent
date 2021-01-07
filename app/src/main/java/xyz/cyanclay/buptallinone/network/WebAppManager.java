package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public class WebAppManager extends SiteManager {


    public WebAppManager(NetworkManager nm, Context context) {
        super(nm, context);
    }

    public Bitmap getCalendar() throws IOException {
        Connection.Response res = nm.getNoVPN(Jsoup.connect("http://wx.bupt.edu.cn/p/calendar"));
        Document dom = res.parse();

        Element img = dom.getElementsByTag("img").get(0);
        Connection.Response resImg = nm.getNoVPN(Jsoup.connect(img.attr("src"))
                .ignoreContentType(true));
        return BitmapFactory.decodeStream(resImg.bodyStream());
    }

    @Override
    protected LoginStatus doLogin() throws Exception {
        return null;
    }

    @Override
    protected LoginStatus doCaptchaLogin(String captcha) throws Exception {
        return null;
    }
}
