package xyz.cyanclay.buptallinone.login;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;


public class JwxtManager {

    private VPNManager vpn;
    private String jwUser;
    private String jwPass;
    private String jwCap;

    private static final String myURL = "https://my.bupt.edu.cn/",
                                jwMainURL = "https://jwxt.bupt.edu.cn/",
                                jwCapURL = jwMainURL + "validateCodeAction.do?gp-1&random=",
                                jwLoginURL = jwMainURL + "loginAction.do",
                                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36",
                                host = "vpn.bupt.edu.cn";
    private static String sessionName = "JSESSIONID";
    private String sessionID = "";
    private String GP_SESSION_CK = "";
    private String PAN_GP_CK_VER = "";
    private String PAN_GP_CACHE_LOCAL_VER_ON_SERVER = "";
    private String GP_CLIENT_CK_UPDATES = "";
    private String PAN_GP_CK_VER_ON_CLIENT= "";


    public JwxtManager(VPNManager vpn){
        this.vpn = vpn;
    }

    public JwxtManager(String jwUser, String pass, String jwCap){
        this.jwUser = jwUser;
        this.jwPass = pass;
        this.jwCap = jwCap;
    }

    public void setJwDetails(String user, String pass, String cap){
        this.jwUser = user;
        this.jwPass = pass;
        this.jwCap = cap;
    }

    public void init(){
        try {
            Connection.Response res = Jsoup.connect(jwMainURL)
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .execute();
                sessionID = res.cookie(sessionName);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void refreshVPNJwCookie(){
        if (!vpn.hasVPNCookie()) vpn.refreshVPNCookie();
        try {
            Connection.Response response = Jsoup.connect(vpn.proxyURL(jwMainURL))
                    .method(Connection.Method.GET)
                    .userAgent(userAgent)
                    .header("Host", host)
                    .cookie("PHPSESSID", vpn.vpnPhpID)
                    .cookie("GP_SESSION_CK", GP_SESSION_CK)
                    .execute();
            Log.w("", response.body());
            PAN_GP_CK_VER = response.header("Set-Cookie").split(",")[0].split(";")[0].trim();
            PAN_GP_CACHE_LOCAL_VER_ON_SERVER = response.header("Set-Cookie").split(",")[1].split(";")[0].trim();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Drawable getCapImage(){
        Drawable capImageDrawable;
        if (vpn.isSchoolNet){
            if (sessionID == null || sessionID.length() == 0){
                init();
            }
            try {
                Connection.Response cap = Jsoup.connect(jwCapURL)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .cookie(sessionName, sessionID)
                        .referrer("https://jwxt.bupt.edu.cn/")
                        .execute();
                capImageDrawable = Drawable.createFromStream(new ByteArrayInputStream(cap.bodyAsBytes()), "");
                capImageDrawable.setVisible(true, true);
            } catch (Exception e){
                e.printStackTrace();
                capImageDrawable = null;
            }
        } else {
            if (GP_SESSION_CK == null || PAN_GP_CK_VER == null || PAN_GP_CK_VER.length() == 0){
                refreshVPNJwCookie();
            }
            try {
                Connection.Response cap = Jsoup.connect(vpn.proxyURL(jwCapURL))
                        .method(Connection.Method.GET)
                        .cookie("PHPSESSID", vpn.vpnPhpID)
                        .cookie("GP_SESSION_CK", GP_SESSION_CK)
                        .cookie("PAN_GP_CK_VER", PAN_GP_CK_VER)
                        .cookie("PAN_GP_CACHE_LOCAL_VER_ON_SERVER", PAN_GP_CACHE_LOCAL_VER_ON_SERVER)
                        .referrer(vpn.proxyURL(jwMainURL))
                        .header("host", "vpn.bupt.edu.cn")
                        .execute();
                capImageDrawable = Drawable.createFromStream(new ByteArrayInputStream(cap.bodyAsBytes()), "");
                capImageDrawable.setVisible(true, true);
                Log.w("", cap.body());
            } catch (Exception e){
                e.printStackTrace();
                capImageDrawable = null;
            }

        }

        return capImageDrawable;
    }

    public String jwLogin(){
        Map<String, String> loginData = new HashMap<String, String>(){{
            put("type", "sso");
            put("zjh", jwUser);
            put("mm", jwPass);
            put("v_yzm", jwCap);
            put("Input2", "");
        }};
        if (vpn.isSchoolNet){
            try {
                Log.w("\nSuccess get session:" , sessionID.trim());
                Connection.Response login = Jsoup.connect(jwLoginURL)
                        .method(Connection.Method.POST)
                        .referrer("https://jwxt.bupt.edu.cn/")
                        .header("origin", "https://jwxt.bupt.edu.cn")
                        .header("content-type", "application/x-www-form-urlencoded")
                        .header("content-length", "55")
                        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                        .header("accept-encoding", "gzip, deflate, br")
                        .header("accept-language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                        .header("cache-control", "max-age=0")
                        .header("upgrade-insecure-requests", "1")
                        .postDataCharset("GBK")
                        .ignoreContentType(true)
                        .userAgent(userAgent)
                        .cookie(sessionName, sessionID)
                        .data(loginData)
                        .execute();
                Log.i("\n\nLogin Webpage Info：\n", login.body().trim());
                Log.i("\n\nLogin URL: ", login.url().toString());

                Connection.Response res = Jsoup.connect(jwMainURL)
                        .header("cookie", sessionName + "=" + sessionID)
                        .method(Connection.Method.GET)
                        .followRedirects(true)
                        .userAgent("Chrome/73.0.3683.103")
                        .execute();
                //Log.w("Response: \n", res.body());
                return login.body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } else {
            try {
                Log.w("VPN_PHPSESSID", vpn.vpnPhpID);
                Log.w("GP_SESSION_CK", GP_SESSION_CK);
                Connection.Response login = Jsoup.connect(vpn.proxyURL(jwLoginURL))
                        .method(Connection.Method.POST)
                        .userAgent(userAgent)
                        .referrer(vpn.proxyURL(jwMainURL))
                        .header("Host", host)
                        .header("Origin", vpn.vpnURL)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .cookie("PHPSESSID", vpn.vpnPhpID)
                        .cookie("GP_SESSION_CK", GP_SESSION_CK)
                        .cookie("PAN_GP_CK_VER", PAN_GP_CK_VER)
                        .cookie("PAN_GP_CACHE_LOCAL_VER_ON_SERVER", PAN_GP_CACHE_LOCAL_VER_ON_SERVER)
                        .data(loginData)
                        .execute();
                return login.body();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

}
