package xyz.cyanclay.buptallinone.network.login;

public enum LoginStatus {
    LOGIN_SUCCESS,
    CAPTCHA_REQUIRED,
    EMPTY_USERNAME,
    EMPTY_PASSWORD,
    INCORRECT_DETAIL,
    EMPTY_CAPTCHA,
    INCORRECT_CAPTCHA,
    UNKNOWN_ERROR,
    TIMED_OUT;

    public String errorMsg = "";
}
