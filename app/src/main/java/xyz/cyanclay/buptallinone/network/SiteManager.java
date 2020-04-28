package xyz.cyanclay.buptallinone.network;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public abstract class SiteManager {

    protected String user;
    protected String pass;
    public Context context;
    protected Map<String, String> cookies;
    public boolean isLoggedIn;
    protected NetworkManager nm;

    public SiteManager(NetworkManager nm, Context context) {
        this.nm = nm;
        this.context = context;
    }

    public void setDetails(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public LoginStatus login() throws IOException {
        Log.i("BUPTAllInOne", this.toString() + "is trying to login with " + user + pass);
        if (user == null) return LoginStatus.EMPTY_USERNAME;
        if (pass == null) return LoginStatus.EMPTY_PASSWORD;
        return doLogin();
    }

    public abstract LoginStatus doLogin() throws IOException;

    protected boolean checkLogin() throws IOException {
        if (isLoggedIn & cookies != null) {
            return true;
        } else return login() == LoginStatus.LOGIN_SUCCESS;
    }


}
