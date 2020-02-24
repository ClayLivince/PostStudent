package xyz.cyanclay.buptallinone.network;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static xyz.cyanclay.buptallinone.network.NetworkManager.parseTask;


public class JwxtManager {

    private NetworkManager networkManager;
    private String jwUser;
    private String jwPass;
    private String jwCap;

    private static final String jwMainURL = "https://jwxt.bupt.edu.cn",
                                jwCapURL = jwMainURL + "validateCodeAction.do?gp-1&random=",
                                jwLoginURL = jwMainURL + "jwLoginAction.do";
    private static String sessionName = "JSESSIONID";
    private String sessionID = "";

    public JwxtManager(NetworkManager nm){
        this.networkManager = nm;
    }

    public JwxtManager(String jwUser, String pass, String jwCap){
        this.jwUser = jwUser;
        this.jwPass = pass;
        this.jwCap = jwCap;
    }

    public void setJwDetails(String user, String pass){
        this.jwUser = user;
        this.jwPass = pass;
    }

    public void setJwCap(String cap){
        this.jwCap = cap;
    }

    public void init(){
        try {
            Connection init = Jsoup.connect(jwMainURL)
                        .method(Connection.Method.GET)
                        .userAgent(NetworkManager.userAgent);
            Connection.Response res = NetworkManager.networkTask(init);
            sessionID = res.cookie(sessionName);
            Log.println(Log.DEBUG, "JwxtSession: " , sessionID.trim());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Drawable getCapImage(){
        Drawable capImageDrawable = null;
        if (networkManager.isSchoolNet){
            if (sessionID == null || sessionID.length() == 0){
                init();
            }
            try {
                Connection cap = Jsoup.connect(jwCapURL)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .cookie(sessionName, sessionID);
                Connection.Response capRes = NetworkManager.networkTask(cap);
                Log.println(Log.DEBUG, "Success get session:" , sessionID.trim());
                capImageDrawable = Drawable.createFromStream(new ByteArrayInputStream(capRes.bodyAsBytes()), "");
                capImageDrawable.setVisible(true, true);
            } catch (Exception e){
                e.printStackTrace();
                capImageDrawable = null;
            }
        }

        return capImageDrawable;
    }

    public LoginStatus jwLogin(){
        Map<String, String> loginData = new HashMap<String, String>(){{
            put("type", "sso");
            put("zjh", jwUser.trim());
            put("mm", jwPass.trim());
            put("v_yzm", jwCap.trim());
            put("Input2", "");
        }};
        Log.w("Information: ", loginData.toString());
        if (networkManager.vpnManager.isSchoolNet){
            try {
                Log.w("session:" , sessionID.trim());
                Connection conn = Jsoup.connect(jwLoginURL)
                        .method(Connection.Method.POST)
                        .referrer("https://jwxt.bupt.edu.cn/")
                        .header("Origin", "https://jwxt.bupt.edu.cn")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Connection", "keep-alive")
                        .ignoreContentType(true)
                        .cookie(sessionName, sessionID)
                        .data(loginData)
                        .followRedirects(true);
                Connection.Response res = NetworkManager.networkTask(conn);
                if (res != null) {
                    Document login = parseTask(res);
                    Log.w("Login Webpage Info：\n", login.outerHtml());
                    Log.w("Login URL: ", login.baseUri());
                    if (login.title().equals("学分制综合教务")){
                        return LoginStatus.LOGIN_SUCCESS;
                    } else {
                        if (login.getElementsByTag("strong").hasText()){
                            if (login.getElementsByTag("strong").first().children().first().text().contains("你输入的校验码有误，请重新输入")){
                                return LoginStatus.INCORRECT_CAPTCHA;
                            } else if (login.getElementsByTag("strong").first().children().first().text().contains("密码")){
                                return LoginStatus.INCORRECT_DETAIL;
                            } else if (login.getElementsByTag("strong").first().children().first().text().contains("证件号")){
                                return LoginStatus.INCORRECT_DETAIL;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } return LoginStatus.UNKNOWN_ERROR;
    }

    public Document checkScore(){
        if (networkManager.isSchoolNet) {
            try {
                Log.w("session:", sessionID.trim());
                Document score = Jsoup.connect("https://jwxt.bupt.edu.cn/gradeLnAllAction.do?type=ln&oper=qbinfo&lnxndm=")
                        .method(Connection.Method.GET)
                        .referrer("https://jwxt.bupt.edu.cn/")
                        .header("Origin", "https://jwxt.bupt.edu.cn")
                        .header("Connection", "keep-alive")
                        .ignoreContentType(true)
                        .cookie(sessionName, sessionID)
                        .get();
                Log.w("Score Info：\n", score.outerHtml());
                Log.w("Login URL: ", score.baseUri());
                Log.w("Score :", processScore(score).toString());

                return score;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public Map<String, String> processScore(Document dom){
        Map<String, String> scoreMap = new HashMap<>();

        Elements odds = dom.getElementsByClass("odd");
        Elements evens = dom.getElementsByClass("even");
        for (Element element : evens) {
            Log.println(Log.DEBUG, "sth", element.child(3).text());
            scoreMap.put(element.child(2).ownText(), element.child(6).child(0).ownText());
        }
        for (Element element : odds) {
            Log.println(Log.DEBUG, "sth", element.child(3).text());
            scoreMap.put(element.child(2).ownText(), element.child(6).child(0).ownText());
        }

        return scoreMap;
    }
}
/*
    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng, * / *;q=0.8,application/signed-exchange;v=b3")
        .header("accept-encoding", "gzip, deflate, br")1
        .header("accept-language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
        .header("cache-control", "max-age=0")
        .header("upgrade-insecure-requests", "1")
 */
