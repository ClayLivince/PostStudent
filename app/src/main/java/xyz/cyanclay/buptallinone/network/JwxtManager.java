package xyz.cyanclay.buptallinone.network;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public class JwxtManager extends SiteManager {

    private static final String jwMainURL = "https://jwxt.bupt.edu.cn/",
            jwCapURL = jwMainURL + "validateCodeAction.do?gp-1&random=",
            jwLoginURL = jwMainURL + "jwLoginAction.do";

    private String jwCap;


    JwxtManager(NetworkManager nm) {
        super(nm);
    }

    public void setJwCap(String cap) {
        this.jwCap = cap;
    }

    private void init() {
        try {
            Connection.Response res = nm.get(jwMainURL);
            cookies = res.cookies();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Drawable getCapImage() throws IOException {
        Drawable capImageDrawable = null;
        if (checkLogin()) {
            try {
                Connection.Response cap = nm.get(jwCapURL, cookies, true);
                capImageDrawable = Drawable.createFromStream(new ByteArrayInputStream(cap.bodyAsBytes()), "");
                capImageDrawable.setVisible(true, true);
            } catch (Exception e) {
                e.printStackTrace();
                capImageDrawable = null;
            }
        }

        return capImageDrawable;
    }

    @Override
    public LoginStatus doLogin() throws IOException {
        if (user == null) return LoginStatus.EMPTY_USERNAME;
        if (pass == null) return LoginStatus.EMPTY_PASSWORD;
        if (jwCap == null) return LoginStatus.EMPTY_CAPTCHA;
        Map<String, String> loginData = new HashMap<String, String>() {{
            put("type", "sso");
            put("zjh", user.trim());
            put("mm", pass.trim());
            put("v_yzm", jwCap.trim());
            put("Input2", "");
        }};

        Connection.Response res = nm.post(Jsoup.connect(jwLoginURL)
                .cookies(cookies)
                .data(loginData));
        if (res != null) {
            Document login = res.parse();

            if (login.title().equals("学分制综合教务")) {
                return LoginStatus.LOGIN_SUCCESS;
            } else {
                if (login.getElementsByTag("strong").hasText()) {
                    if (login.getElementsByTag("strong").first().children().first().text().contains("你输入的校验码有误，请重新输入")) {
                        return LoginStatus.INCORRECT_CAPTCHA;
                    } else if (login.getElementsByTag("strong").first().children().first().text().contains("密码")) {
                        return LoginStatus.INCORRECT_DETAIL;
                    } else if (login.getElementsByTag("strong").first().children().first().text().contains("证件号")) {
                        return LoginStatus.INCORRECT_DETAIL;
                    }
                }
            }
        }

        return LoginStatus.UNKNOWN_ERROR;
    }

    public Document checkScore() throws IOException {

        Document score = nm.get("https://jwxt.bupt.edu.cn/gradeLnAllAction.do?type=ln&oper=qbinfo&lnxndm=",
                cookies).parse();
        Log.w("Score Info：\n", score.outerHtml());
        Log.w("Login URL: ", score.baseUri());
        Log.w("Score :", processScore(score).toString());

        return score;

    }

    private Map<String, String> processScore(Document dom) {
        Map<String, String> scoreMap = new HashMap<>();

        Elements odds = dom.getElementsByClass("odd");
        Elements evens = dom.getElementsByClass("even");
        for (Element element : evens) {
            scoreMap.put(element.child(2).ownText(), element.child(6).child(0).ownText());
        }
        for (Element element : odds) {
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
