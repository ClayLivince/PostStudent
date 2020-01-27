package xyz.cyanclay.buptallinone.login;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;

public class VPNManager {

    private String vpnUser;
    private String vpnPass;

    public boolean isSchoolNet = true;

    static final String vpnURL = "https://vpn.bupt.edu.cn/",
            vpnLoginURL = "https://vpn.bupt.edu.cn/global-protect/login.esp",
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36",
            host = "vpn.bupt.edu.cn";
    public String vpnPhpID = "";
    private String GP_SESSION_CK = "";
    private String PAN_GP_CK_VER = "";
    private String PAN_GP_CACHE_LOCAL_VER_ON_SERVER = "";
    private String GP_CLIENT_CK_UPDATES = "";
    private String PAN_GP_CK_VER_ON_CLIENT= "";

    public void setVpnDetails(String user, String pass){
        this.vpnUser = user;
        this.vpnPass = pass;
    }

    public void refreshVPNCookie(){
        try {
            Connection.Response init = Jsoup.connect(vpnLoginURL)
                    .method(Connection.Method.GET)
                    .userAgent(userAgent)
                    .header("Host", host)
                    .referrer(vpnURL)
                    .postDataCharset("utf8")
                    .execute();
            vpnPhpID = init.header("Set-Cookie").split(";")[0];

            Map<String, String> vpnLoginData = new HashMap<String, String>(){{
                put("prot", "https");
                put("server", host);
                put("inputstr", "");
                put("action", "getsoftware");
                put("user", vpnUser);
                put("passwd", vpnPass);
                put("ok", "Log In");
            }};
            Connection.Response vpnLogin = Jsoup.connect(vpnLoginURL)
                    .method(Connection.Method.POST)
                    .userAgent(userAgent)
                    .referrer(vpnLoginURL)
                    .header("Host", host)
                    .header("Origin", vpnURL)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .ignoreContentType(true)
                    .cookie("PHPSESSID", vpnPhpID)
                    .data(vpnLoginData)
                    .execute();
            GP_SESSION_CK = vpnLogin.header("Set-Cookie").split(";")[0];

            Log.w("VPN_PHPSESSID", vpnPhpID);
            Log.w("GP_CK", GP_SESSION_CK);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean hasVPNCookie(){
        return (vpnPhpID == null || GP_SESSION_CK == null);
    }

    private String transformURL(String URL){
        return URL.replaceFirst(":/", "");
    }

    public String proxyURL(String URL){
        Log.w("Proxied URL:", vpnURL + transformURL(URL));
        return vpnURL + transformURL(URL);
    }
}
