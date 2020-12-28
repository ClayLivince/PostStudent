package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public class AuthManager extends SiteManager {

    private static final String loginURL = "https://auth.bupt.edu.cn/authserver/login";
    private Map<String, String> cachedDetails;

    AuthManager(NetworkManager nm, Context context) {
        super(nm, context);
    }

    public LoginStatus login(String service) throws IOException, LoginException {
        return login(service, null);
    }

    public LoginStatus login(String service, String captcha) throws IOException, LoginException {
        String url = loginURL + "?service=" + getEncodedURL(service);
        Connection.Response authInit = nm.get(url);
        String authInitURL = authInit.url().toString();
        cookies = authInit.cookies();

        if (!(authInitURL.contains("auth.bupt.edu.cn") |
                authInitURL.contains("77726476706e69737468656265737421f1e2559469327d406a468ca88d1b203b"))) {
            setLoggedIn(true);
            return LoginStatus.LOGIN_SUCCESS;
        }

        final Document auth = authInit.parse();
        LoginStatus challenge = judgeCaptcha(auth, captcha);
        if (challenge != null) return challenge;
        else return loginWithMap(cachedDetails, service);
    }

    private LoginStatus judgeCaptcha(final Document target, String sCaptcha) throws IOException, LoginException {
        Map<String, String> details = new HashMap<String, String>() {{
            put("username", user);
            put("password", pass);
            put("lt", target.getElementsByAttributeValue("name", "lt").first().attr("value"));
            put("execution", target.getElementsByAttributeValue("name", "execution").first().attr("value"));
            put("_eventId", target.getElementsByAttributeValue("name", "_eventId").first().attr("value"));
            put("rmShown", target.getElementsByAttributeValue("name", "rmShown").first().attr("value"));
        }};
        if (sCaptcha != null) details.put("captchaResponse", sCaptcha);
        this.cachedDetails = details;
        Element captchaDiv = target.getElementById("casCaptcha");
        if (!captchaDiv.children().isEmpty()) {
            Element eCaptchaImg = captchaDiv.getElementById("captchaImg");
            if (eCaptchaImg != null) {
                Connection.Response captchaRes = nm.get(Jsoup.connect(eCaptchaImg.absUrl("src"))
                        .ignoreContentType(true), cookies);
                Drawable captchaImg = Drawable.createFromStream(captchaRes.bodyStream(), "AuthCaptcha");
                LoginStatus captcha = LoginStatus.CAPTCHA_REQUIRED;
                captcha.site = this;
                captcha.captchaImage = captchaImg;
                return captcha;
            }
        }
        return null;
    }

    public LoginStatus loginWithCaptcha(String service, String captcha) throws IOException, LoginException {
        if (cachedDetails != null) {
            cachedDetails.put("captchaResponse", captcha);
            return loginWithMap(cachedDetails, service);
        } else {
            return login(service, captcha);
        }
    }

    private LoginStatus loginWithMap(Map<String, String> details, String service) throws IOException, LoginException {
        String url = loginURL + "?service=" + getEncodedURL(service);
        Connection.Response authLogin = nm.post(Jsoup.connect(url)
                .cookies(cookies)
                .data(details));

        String authLoginUrl = authLogin.url().toString();
        cookies.putAll(authLogin.cookies());
        final Document login = Jsoup.parse(authLogin.body());
        if (login.getElementsByClass("errors").first() != null) {
            String status = login.getElementsByClass("errors").first().ownText();
            if (status != null) {
                if (status.equals("Username is a required field.")) {
                    return LoginStatus.EMPTY_USERNAME;
                } else if (status.equals("Password is a required field.")) {
                    return LoginStatus.EMPTY_PASSWORD;
                } else if (status.equals("The username or password you provided cannot be determined to be authentic.")) {
                    return LoginStatus.INCORRECT_DETAIL;
                } else if (status.equals("Please enter captcha.")) {
                    return LoginStatus.EMPTY_CAPTCHA;
                } else if (status.equals("invalid captcha.")) {
                    LoginStatus challenge = judgeCaptcha(login, null);
                    if (challenge != null) return challenge;
                } else if (status.length() != 0) {
                    return LoginStatus.UNKNOWN_ERROR;
                }
            }
        } else {
            URL serviceURL = new URL(service);
            if (authLoginUrl.contains(serviceURL.getHost()) |
                    authLoginUrl.contains(nm.vpnManager.analyseURL(serviceURL.getProtocol() + "://" + serviceURL.getHost()))) {
                setLoggedIn(true);
                return LoginStatus.LOGIN_SUCCESS;
            } else {
                LoginStatus challenge = judgeCaptcha(login, null);
                if (challenge != null) return challenge;
            }
        }
        return LoginStatus.UNKNOWN_ERROR;
    }

    private String getEncodedURL(String service) {
        try {
            return URLEncoder.encode(service, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return service;
        }
    }

    @Override
    public void setLoggedIn(boolean in) {
        super.setLoggedIn(in);
        nm.infoManager.setLoggedIn(in);
    }

    /**
     * Don't Use This Method! Empty Shell!
     *
     * @return Definitely null
     */
    @Override
    @Deprecated
    protected LoginStatus doLogin() {
        return null;
    }

    /**
     * Don't Use This Method! Empty Shell!
     *
     * @return Definitely null
     */
    @Override
    @Deprecated
    protected LoginStatus doCaptchaLogin(String captcha) {
        return null;
    }
}
