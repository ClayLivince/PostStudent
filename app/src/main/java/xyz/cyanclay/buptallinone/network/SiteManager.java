package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public abstract class SiteManager {

    public String user;
    protected String pass;
    public Context context;
    protected Map<String, String> cookies = new HashMap<>();
    private boolean isLoggedIn;
    protected NetworkManager nm;

    public SiteManager(NetworkManager nm, Context context) {
        this.nm = nm;
        this.context = context;
    }

    public void setDetails(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public LoginStatus login() throws IOException, LoginException {
        Log.i("BUPTAllInOne", this.toString() + "is trying to login with " + user + pass);
        if (user == null) return LoginStatus.EMPTY_USERNAME;
        if (pass == null) return LoginStatus.EMPTY_PASSWORD;
        return doLogin();
    }

    public LoginStatus login(String captcha) throws IOException, LoginException {
        Log.i("BUPTAllInOne", this.toString() + "is trying to login with " + user + pass + captcha);
        if (user == null) return LoginStatus.EMPTY_USERNAME;
        if (pass == null) return LoginStatus.EMPTY_PASSWORD;
        return doCaptchaLogin(captcha);
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    protected abstract LoginStatus doLogin() throws IOException, LoginException;

    protected abstract LoginStatus doCaptchaLogin(final String captcha) throws IOException, LoginException;

    protected boolean checkLogin() throws IOException, LoginException {
        if (isLoggedIn & cookies != null) {
            return true;
        } else {
            LoginStatus login = login();
            if (login.equals(LoginStatus.LOGIN_SUCCESS))
                return true;
            else throw new LoginException(this, login);
        }
    }
}
