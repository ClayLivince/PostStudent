package xyz.cyanclay.buptallinone.login;

import android.graphics.drawable.Drawable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;


public class LoginManager {

    private String user;
    private String pass;
    private String cap;

    private static final String mainURL = "https://jwxt.bupt.edu.cn/";
    private static final String jwcapURL = mainURL + "validateCodeAction.do?random=";
    private static final String loginURL = mainURL + "loginAction.do";
    private static String sessionName = "JSESSIONID";
    private String sessionID = "";

    private CookieManager manager = new CookieManager();

    public LoginManager(){
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(manager);
    }

    public LoginManager(String user, String pass, String cap){
        this.user = user;
        this.pass = pass;
        this.cap = cap;
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(manager);
    }

    public void setLoginDetails(String user, String pass, String cap){
        this.user = user;
        this.pass = pass;
        this.cap = cap;
    }

    public void init(){
        try {
            Connection.Response res = Jsoup.connect(mainURL).method(Connection.Method.GET).execute();
            sessionID = res.cookie(sessionName);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Drawable getCapImage(){
        Drawable capImageDrawable;
        if (sessionID == null || sessionID.length() == 0){
            init();
        }
        try {
            Connection.Response cap = Jsoup.connect(jwcapURL)
                    .header("Cookie", sessionName + "=" + sessionID)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .cookie(sessionName, sessionID)
                    .execute();
            capImageDrawable = Drawable.createFromStream(new ByteArrayInputStream(cap.bodyAsBytes()), "");
            capImageDrawable.setVisible(true, true);
        } catch (Exception e){
            e.printStackTrace();
            capImageDrawable = null;
        }
        return capImageDrawable;
    }

    public boolean login(){
        try {
            System.out.print("Successfully get sessionId : " + sessionID);
            Connection.Response login = Jsoup.connect(loginURL)
                    .header("Cookie", sessionName + "=" + sessionID)  //携带刚才的 Cookie 信息
                    .data("type", "sso", "zjh", user, "mm", pass, "v_yzm", cap)
                    .cookie(sessionName, sessionID)
                    .method(Connection.Method.POST)
                    .execute();
            return (login.body().contains("综合教务"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getSessionID(){
        return sessionID;
    }
}
