package xyz.cyanclay.buptallinone.data;

import android.graphics.drawable.Drawable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import xyz.cyanclay.buptallinone.data.model.LoggedInUser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class RemoteLoginManager {

    private static final String mainURL = "https://jwxt.bupt.edu.cn/";
    private static final String jwcapURL = mainURL + "validateCodeAction.do?random=";
    private static final String loginURL = mainURL + "loginAction.do";
    private static String sessionName = "JSESSIONID";
    private String sessionID = "";

    public Result<LoggedInUser> login(String username, String password, String captcha) {

        try {
            // TODO: handle loggedInUser authentication

            Connection.Response login = Jsoup.connect(loginURL)
                    .header("Cookie", sessionName + "=" + sessionID)  //携带刚才的 Cookie 信息
                    .data("type", "sso", "zjh", username, "mm", password, "v_yzm", captcha)
                    //这里的 zjh 和 mm 就是登录页面 form 表单的 name
                    .method(Connection.Method.POST)
                    .cookie(sessionName, sessionID)
                    .execute();
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            username,
                            username);
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
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
}
