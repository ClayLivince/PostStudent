package xyz.cyanclay.poststudent.ui.userdetails;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.net.SocketTimeoutException;

import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.JwxtManager;
import xyz.cyanclay.poststudent.network.SiteManager;
import xyz.cyanclay.poststudent.network.VPNManager;
import xyz.cyanclay.poststudent.network.info.InfoManager;
import xyz.cyanclay.poststudent.network.jwgl.JwglManager;
import xyz.cyanclay.poststudent.network.login.LoginException;
import xyz.cyanclay.poststudent.network.login.LoginStatus;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;

class VerifyTask {

    static void verify(final UserDetailsFragment udf,
                       final SiteManager site) {
        verify(udf, site, null);
    }

    static void verify(final UserDetailsFragment udf,
                       final SiteManager site, final String captcha) {
        final TextView verify;
        final EditText input;
        final ProgressBar progress;
        if (site instanceof VPNManager) {
            verify = udf.root.findViewById(R.id.textViewVPNVerify);
            input = udf.root.findViewById(R.id.inpVPNPass);
            progress = udf.root.findViewById(R.id.progressBarVPN);
        } else if (site instanceof InfoManager) {
            verify = udf.root.findViewById(R.id.textViewInfoVerify);
            input = udf.root.findViewById(R.id.inpInfoPass);
            progress = udf.root.findViewById(R.id.progressBarInfoPass);
        } else if (site instanceof JwglManager) {
            verify = udf.root.findViewById(R.id.textViewJwglVerify);
            input = udf.root.findViewById(R.id.inpJwglPass);
            progress = udf.root.findViewById(R.id.progressBarJwglPass);
        } else if (site instanceof JwxtManager) {
            verify = udf.root.findViewById(R.id.textViewJwxtVerify);
            input = udf.root.findViewById(R.id.inpJwxtPass);
            progress = udf.root.findViewById(R.id.progressBarJwxtPass);
        } else {
            verify = udf.root.findViewById(R.id.textViewVPNVerify);
            input = udf.root.findViewById(R.id.inpVPNPass);
            progress = udf.root.findViewById(R.id.progressBarVPN);
        }

        verify.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        site.setDetails(udf.id, input.getText().toString());

        new TryAsyncTask<Void, Void, LoginStatus>() {
            LoginException exception;

            @Override
            protected LoginStatus doInBackground(Void... voids) {
                try {
                    if (captcha != null) return site.login(captcha);
                    return site.login();
                } catch (LoginException e) {
                    exception = e;
                    return e.status;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) return LoginStatus.TIMED_OUT;
                    LoginStatus error = LoginStatus.UNKNOWN_ERROR;
                    error.errorMsg = e.toString();
                    return error;
                }
            }

            @Override
            protected void postExecute(LoginStatus loginStatus) {
                progress.setVisibility(View.INVISIBLE);
                switch (loginStatus) {
                    case LOGIN_SUCCESS: {
                        verifySuccess(verify);
                        break;
                    }
                    case EMPTY_USERNAME:
                    case EMPTY_PASSWORD:
                    case INCORRECT_DETAIL: {
                        verifyFailed(verify, input);
                        Snackbar.make(udf.root, R.string.incorrect_password, Snackbar.LENGTH_LONG).show();
                        break;
                    }
                    case TIMED_OUT: {
                        verifyFailed(verify, input);
                        Snackbar.make(udf.root, R.string.timed_out, Snackbar.LENGTH_LONG).show();
                        break;
                    }
                    case EMPTY_CAPTCHA:
                    case INCORRECT_CAPTCHA:
                    case CAPTCHA_REQUIRED: {
                        if (exception != null)
                            udf.popupCaptcha(loginStatus.captchaImage
                                    , exception.site);
                        else udf.popupCaptcha(loginStatus.captchaImage, loginStatus.site);
                        break;
                    }
                    case TOO_MANY_ERRORS: {
                        Snackbar.make(udf.root, R.string.vpn_too_many_errors, BaseTransientBottomBar.LENGTH_LONG).show();
                        break;
                    }
                    case UNKNOWN_ERROR: {
                        verifyFailed(verify, input);
                        Snackbar.make(udf.root, "发生错误： " + loginStatus.errorMsg, Snackbar.LENGTH_LONG).show();
                    }
                    default: {
                        verifyFailed(verify, input);
                        Snackbar.make(udf.root, R.string.unknown_error, Snackbar.LENGTH_LONG).show();
                    }
                }
                udf.deterVisibility();
            }
        }.execute();
    }

    static synchronized void verifyFailed(TextView verify, EditText inp) {
        verify.setText("× 验证失败");
        verify.setTextColor(Color.parseColor("#E03030"));
        verify.setVisibility(View.VISIBLE);
        inp.setFocusableInTouchMode(true);
        inp.requestFocus();
    }

    static synchronized void verifySuccess(TextView verify) {
        verify.setText("√ 验证成功");
        verify.setTextColor(Color.parseColor("#00C020"));
        verify.setVisibility(View.VISIBLE);
    }

}
