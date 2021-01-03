package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.util.Log;

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
    public NetworkManager nm;

    public SiteManager(NetworkManager nm, Context context) {
        this.nm = nm;
        this.context = context;
    }

    public void setDetails(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public synchronized LoginStatus login() throws Exception {
        Log.i("BUPTAllInOne", this.toString() + "is trying to login with " + user + pass);
        return checkLoginStatus(null);
    }

    public synchronized LoginStatus login(String captcha) throws Exception {
        Log.i("BUPTAllInOne", this.toString() + "is trying to login with " + user + pass + captcha);
        return checkLoginStatus(captcha);
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    protected abstract LoginStatus doLogin() throws Exception;

    protected abstract LoginStatus doCaptchaLogin(final String captcha) throws Exception;

    protected synchronized LoginStatus checkLoginStatus(String captcha) throws Exception {
        if (user == null) return LoginStatus.EMPTY_USERNAME;
        if (pass == null) return LoginStatus.EMPTY_PASSWORD;
        if (isLoggedIn & !cookies.isEmpty()) {
            return LoginStatus.LOGIN_SUCCESS;
        } else {
            if (captcha == null)
                return doLogin();
            else return doCaptchaLogin(captcha);
        }
    }

    protected synchronized boolean checkLogin() throws Exception {
        if (isLoggedIn & !cookies.isEmpty()) {
            return true;
        } else {
            LoginStatus login = login();
            if (login.equals(LoginStatus.LOGIN_SUCCESS))
                return true;
            else throw new LoginException(this, login);
        }
    }
}
