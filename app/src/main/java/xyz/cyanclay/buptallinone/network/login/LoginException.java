package xyz.cyanclay.buptallinone.network.login;

import java.io.IOException;

import xyz.cyanclay.buptallinone.network.SiteManager;

public class LoginException extends IOException {
    SiteManager site;
    String url;

    public LoginException(SiteManager site, String url) {
        this.site = site;
        this.url = url;
    }

    public String toString() {
        return "LoginException : "
                + "\nSite : " + site.toString()
                + "\nURL : " + url
                + "\nCause : " + this.getCause();
    }

}
