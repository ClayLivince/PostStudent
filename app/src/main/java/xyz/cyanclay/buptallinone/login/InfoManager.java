package xyz.cyanclay.buptallinone.login;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.HttpURLConnection;

public class InfoManager {

    private VPNManager vpn;
    private static final String infoURL = "https://my.bupt.edu.cn/",
                                loginURL = "https://auth.bupt.edu.cn/authserver/login?service=http%3A%2F%2Fmy.bupt.edu.cn%2Flogin.portal";

    private String infoUser, infoPass;
    private String infoSession;

    public void refreshInfoCookie(){
        Connection.Response res = Jsoup.connect(loginURL).method(Connection.Method.GET).userAgent(vpn.userAgent).response();
    }

    public String infoLogin(String user, String pass){
        setInfoLoginDetail(user, pass);
        if(infoSession == null || infoSession.length() == 0) refreshInfoCookie();

        try {
            Connection.Response login = Jsoup.connect(loginURL)
                    .method(Connection.Method.POST)
                    .userAgent(vpn.userAgent)
                    .cookie("JSESSIONID", infoSession)
                    .referrer(infoURL)
                    .data("username", infoUser, "password", infoPass).response();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setInfoLoginDetail(String user, String pass){
        this.infoUser = user;
        this.infoPass = pass;
    }
}
