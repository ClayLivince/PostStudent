package xyz.cyanclay.buptallinone.network.login;

import android.graphics.drawable.Drawable;

import xyz.cyanclay.buptallinone.network.SiteManager;

public enum LoginStatus {
    LOGIN_SUCCESS,
    CAPTCHA_REQUIRED,
    EMPTY_USERNAME,
    EMPTY_PASSWORD,
    INCORRECT_DETAIL,
    EMPTY_CAPTCHA,
    INCORRECT_CAPTCHA,
    UNKNOWN_ERROR,
    TOO_MANY_ERRORS,
    TIMED_OUT;

    public String errorMsg = "";
    public Drawable captchaImage;
    public SiteManager site;
}
