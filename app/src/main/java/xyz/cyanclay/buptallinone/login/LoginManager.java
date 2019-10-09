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
        if (sessionID.equals("")){
            init();
        }
        try {
            Connection.Response cap = Jsoup.connect(jwcapURL).method(Connection.Method.GET) .ignoreContentType(true).execute();
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
            Connection.Response login = Jsoup.connect(loginURL)
                    .header("Cookie", sessionName + "=" + sessionID)  //携带刚才的 Cookie 信息
                    .data("type", "sso", "zjh", user, "mm", pass, "v_yzm", cap)
                    //这里的 zjh 和 mm 就是登录页面 form 表单的 name
                    .method(Connection.Method.POST)
                    .execute();
            if(login.body().contains("学分制综合教务")){
                return true;
            } else return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
