package xyz.cyanclay.buptallinone.network;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import xyz.cyanclay.buptallinone.network.login.LoginStatus;

public abstract class SiteManager {

    protected String user;
    protected String pass;
    protected Map<String, String> cookies;
    public boolean isLoggedIn;
    protected NetworkManager nm;

    public SiteManager(NetworkManager nm) {
        this.nm = nm;
    }

    public void setDetails(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public LoginStatus login() throws IOException {
        Log.i("BUPTAllInOne", this.toString() + "is trying to login with " + user + pass);
        return doLogin();
    }

    public abstract LoginStatus doLogin() throws IOException;

    public boolean checkLogin() throws IOException {
        if (isLoggedIn & cookies != null) {
            return true;
        } else return login() == LoginStatus.LOGIN_SUCCESS;
    }


}
