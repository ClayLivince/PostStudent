package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.eclipsesource.v8.V8;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public class VPNManager extends SiteManager {

    private static String vpnURL = "https://webvpn.bupt.edu.cn";

    private String captchaID = "";

    VPNManager(NetworkManager nm, Context context) throws IOException {
        super(nm, context);
    }

    public LoginStatus doLogin() throws IOException {
        Connection.Response initCookie = Jsoup.connect(vpnURL)
                .userAgent(NetworkManager.userAgent)
                .method(Connection.Method.GET)
                .execute();
        cookies = initCookie.cookies();

        Connection.Response init = Jsoup.connect(vpnURL + "/login")
                .userAgent(NetworkManager.userAgent)
                .method(Connection.Method.GET)
                .timeout(5000)
                .execute();
        Document initDoc = init.parse();

        LoginStatus challenge = judgeCaptcha(initDoc);
        if (challenge != null) return challenge;

        Map<String, String> details = new HashMap<String, String>() {{
            put("auth_type", "local");
            put("username", user);
            put("sms_code", "");
            put("password", pass);
        }};

        return loginWithMap(details);
    }

    private LoginStatus judgeCaptcha(Document initDoc) throws IOException {
        boolean needCaptcha = false;
        Elements inputs = initDoc.getElementsByTag("input");
        for (Element input : inputs) {
            if (input.hasAttr("name")) {
                if (input.attr("name").equals("needCaptcha")) {
                    String sNeedCaptcha = input.attr("value");
                    if (sNeedCaptcha.length() != 0) {
                        needCaptcha = Boolean.parseBoolean(sNeedCaptcha);
                        break;
                    }
                }
            }
        }
        if (needCaptcha) {
            try {
                Element eCaptchaID = initDoc.getElementsByClass("captcha-div").first().child(0);
                if (eCaptchaID.attr("name").equals("captcha_id"))
                    captchaID = eCaptchaID.attr("value");
                LoginStatus captchaRequired = LoginStatus.CAPTCHA_REQUIRED;
                captchaRequired.captchaImage = getCaptcha(captchaID);
                captchaRequired.site = this;
                return captchaRequired;
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                LoginStatus error = LoginStatus.UNKNOWN_ERROR;
                error.errorMsg = "Captcha Required But Failed to Read CaptchaID!";
                return error;
            }
        }
        return null;
    }

    @Override
    protected LoginStatus doCaptchaLogin(final String captcha) throws IOException {
        Map<String, String> details = new HashMap<String, String>() {{
            put("auth_type", "local");
            put("username", user);
            put("sms_code", "");
            put("password", pass);
            put("needCaptcha", "true");
            put("captcha_id", captchaID);
            put("captcha", captcha);
        }};
        return loginWithMap(details);
    }

    private LoginStatus loginWithMap(Map<String, String> details) throws IOException {
        String loginURL = vpnURL + "/do-login?local_login=true";
        Connection.Response login = Jsoup.connect(loginURL)
                .cookies(cookies)
                .data(details)
                .userAgent(NetworkManager.userAgent)
                .method(Connection.Method.POST)
                .execute();
        cookies.putAll(login.cookies());
        Document loginDoc = login.parse();

        if (loginDoc.getElementById("aHref") != null) {
            setLoggedIn(true);
            return LoginStatus.LOGIN_SUCCESS;
        } else {
            return checkStatus(loginDoc);
        }
    }

    private LoginStatus checkStatus(Document loginDoc) throws IOException {
        String token = checkLogoutOthers(loginDoc);
        if (token != null) {
            if (token.length() != 0) return logoutOthers(token);
        }
        Elements upperBox = loginDoc.getElementsByClass("upper-login-field");
        if (!upperBox.isEmpty()) {
            Elements msgs = upperBox.first().getElementsByClass("msg");
            if (!msgs.isEmpty()) {
                String status = msgs.first().ownText();
                if (status.contains("错误次数过多")) {
                    return LoginStatus.TOO_MANY_ERRORS;
                } else if (status.contains("验证码错误")) {
                    LoginStatus challenge = judgeCaptcha(loginDoc);
                    if (challenge != null) return challenge;
                } else if (status.contains("用户名密码错误")) {
                    return LoginStatus.INCORRECT_DETAIL;
                } else {
                    LoginStatus error = LoginStatus.UNKNOWN_ERROR;
                    error.errorMsg = status;
                    return error;
                }
            }
        }
        LoginStatus error = LoginStatus.UNKNOWN_ERROR;
        error.errorMsg = upperBox.text();
        return error;
    }

    private String checkLogoutOthers(Document loginDoc) {
        StringBuilder sb = new StringBuilder();
        for (Element entry : loginDoc.getElementsByTag("script")) {
            sb.append(entry.data());
        }

        String token = "";
        String[] vars = sb.toString().split("var");
        for (String var : vars) {
            if (var.contains("logoutOtherToken")) {
                if (var.contains("=")) {
                    String[] kvp = var.split("=");
                    token = kvp[1].split("\n")[0].replace("'", "").trim();
                    break;
                }
            }
        }
        return token;
    }

    private LoginStatus logoutOthers(String token) throws IOException {

        if (!token.equals("")) {
            Connection.Response confirm = Jsoup.connect(vpnURL + "/do-confirm-login")
                    .data("username", user, "logoutOtherToken", token)
                    .method(Connection.Method.POST)
                    .userAgent(NetworkManager.userAgent)
                    .cookies(cookies)
                    .ignoreContentType(true)
                    .execute();

            try {
                JSONObject confirmJSON = new JSONObject(confirm.body());
                if (confirmJSON.getBoolean("success")) {
                    setLoggedIn(true);
                    return LoginStatus.LOGIN_SUCCESS;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new IOException("VPN JSON Exception!");
            }
        }
        setLoggedIn(false);
        return LoginStatus.UNKNOWN_ERROR;
    }

    private Drawable getCaptcha(String captchaID) throws IOException {
        if (captchaID == null) {
            throw new IOException("VPN CaptchaID is null!");
        }
        String captchaURL = "http://webvpn.bupt.edu.cn/captcha/" + captchaID;
        Connection.Response captchaRes = Jsoup.connect(captchaURL)
                .ignoreContentType(true)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .userAgent(NetworkManager.userAgent)
                .execute();
        return Drawable.createFromStream(captchaRes.bodyStream(), "VPNCaptcha");
    }

    public Connection.Response get(Connection conn) throws Exception {
        if (checkLogin()) {
            conn.cookies(cookies);
            String url = conn.request().url().toString();
            if (!url.contains("webvpn.bupt.edu.cn")) conn.url(analyseURL(url));
            Connection.Response res = conn.execute();
            cookies.putAll(res.cookies());
            Log.i("VPNConnection", "Trying to connect to " + res.url().toString());
            return res;
        }
        throw new IOException("VPN Failed to login.");
    }

    public String analyseURL(String url) {
        String protocol = url.split("://", 2)[0];
        String href = url.split("://", 2)[1];
        InputStream is = context.getResources().openRawResource(R.raw.vpn);   //获取用户名与密码加密的js代码
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            V8 runtime = V8.createV8Runtime();
            //使用J2V8运行js代码并将编码结果返回
            final String result = runtime.executeStringScript(sb.toString()
                    + ";encrypUrl('" + protocol + "','" + href + "');\n");

            return vpnURL + result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String packageURL(String href) {
        if (href.charAt(0) == '/')
            href = href.substring(1);
        return vpnURL + '/' + href;
    }

}
