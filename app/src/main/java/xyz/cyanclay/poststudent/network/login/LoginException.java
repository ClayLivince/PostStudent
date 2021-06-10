package xyz.cyanclay.poststudent.network.login;

import xyz.cyanclay.poststudent.network.SiteManager;

public class LoginException extends Exception {
    public SiteManager site;
    public LoginStatus status;

    public LoginException(SiteManager site, LoginStatus status) {
        this.site = site;
        this.status = status;
    }

    public String toString() {
        return "LoginException : "
                + "\nSite : " + site.toString()
                + "\nStatus : " + status.toString()
                + "\nCause : " + this.getCause();
    }

}
