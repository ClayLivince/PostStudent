package xyz.cyanclay.buptallinone.ui.userdetails;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.SocketTimeoutException;

import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.SiteManager;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;

class VerifyTask {

    static void verify(final UserDetailsFragment udf, String user, final View root, final TextView verify, final EditText input, final ProgressBar progress,
                       final SiteManager site) {

        verify.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        site.setDetails(user, input.getText().toString());

        new AsyncTask<Void, Void, LoginStatus>() {
            @Override
            protected LoginStatus doInBackground(Void... voids) {
                try {
                    return site.login();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) return LoginStatus.TIMED_OUT;
                    return LoginStatus.UNKNOWN_ERROR;
                }
            }

            @Override
            protected void onPostExecute(LoginStatus loginStatus) {
                progress.setVisibility(View.INVISIBLE);
                switch (loginStatus) {
                    case LOGIN_SUCCESS: {
                        verifySuccess(verify);
                        break;
                    }
                    case INCORRECT_DETAIL: {
                        verifyFailed(verify, input);
                        Snackbar.make(root, R.string.incorrect_password, Snackbar.LENGTH_LONG).show();
                        break;
                    }
                    case TIMED_OUT: {
                        verifyFailed(verify, input);
                        Snackbar.make(root, R.string.timed_out, Snackbar.LENGTH_LONG);
                        break;
                    }
                    case UNKNOWN_ERROR: {
                        verifyFailed(verify, input);
                        Snackbar.make(root, "发生错误： " + loginStatus.errorMsg, Snackbar.LENGTH_LONG).show();
                    }
                    default: {
                        verifyFailed(verify, input);
                        Snackbar.make(root, R.string.unknown_error, Snackbar.LENGTH_LONG).show();
                    }
                }
                udf.deterVisibility();
            }
        }.execute();
    }

    private static synchronized void verifyFailed(TextView verify, EditText inp) {
        verify.setText("× 验证失败");
        verify.setTextColor(Color.parseColor("#E03030"));
        verify.setVisibility(View.VISIBLE);
        inp.setFocusableInTouchMode(true);
        inp.requestFocus();
    }

    private static synchronized void verifySuccess(TextView verify) {
        verify.setText("√ 验证成功");
        verify.setTextColor(Color.parseColor("#00C020"));
        verify.setVisibility(View.VISIBLE);
    }

}
