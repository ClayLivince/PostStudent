package xyz.cyanclay.buptallinone.network.login;

import android.app.Activity;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.navigation.Navigation;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.net.SocketTimeoutException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.SiteManager;
import xyz.cyanclay.buptallinone.ui.components.TryAsyncTask;

public class LoginTask {

    public static void login(final Activity activity, final View root,
                             final SiteManager site, final String captcha) {
        new TryAsyncTask<Void, Void, LoginStatus>() {
            LoginException exception = null;

            @Override
            protected LoginStatus doInBackground(Void... voids) {
                try {
                    return site.login(captcha);
                } catch (LoginException e) {
                    exception = e;
                    cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) return LoginStatus.TIMED_OUT;
                    else cancel(true);
                }
                return LoginStatus.UNKNOWN_ERROR;
            }

            @Override
            protected void postExecute(LoginStatus status) {
                //super.postExecute(status);
            }

            @Override
            protected void cancelled() throws Exception {
                super.onCancelled();
                if (exception != null) {
                    handleStatus(activity, root, exception.status);
                }
            }

        }.execute();
    }

    public static void handleStatus(Activity activity, View root, LoginStatus status) throws Exception {
        View.OnClickListener listener = new OnSnackbarClickListener(activity);
        switch (status) {
            case EMPTY_USERNAME:
            case EMPTY_PASSWORD: {
                makeSnackbar(root, R.string.empty_details)
                        .setAction(R.string.go_fill_details, listener).show();
                break;
            }
            case INCORRECT_DETAIL: {
                makeSnackbar(root, R.string.incorrect_details)
                        .setAction(R.string.go_fill_details, listener).show();
                break;
            }
            case INCORRECT_CAPTCHA: {
                makeSnackbar(root, R.string.incorrect_captcha).show();
                break;
            }
            case CAPTCHA_REQUIRED:
            case EMPTY_CAPTCHA: {
                makeSnackbar(root, R.string.incorrect_captcha).show();
                ((MainActivity) activity).popupCaptcha(root, status.captchaImage, status.site);
                break;
            }
            case TOO_MANY_ERRORS: {
                makeSnackbar(root, R.string.vpn_too_many_errors).show();
                break;
            }
            case TIMED_OUT: {
                makeSnackbar(root, R.string.timed_out).show();
                break;
            }
            case UNKNOWN_ERROR: {
                makeSnackbar(root, R.string.unknown_error).show();
                break;
            }
        }
    }

    private static Snackbar makeSnackbar(View root, @StringRes int id) throws IllegalArgumentException {
        return Snackbar.make(root, id, Snackbar.LENGTH_LONG);
    }

    private static class OnSnackbarClickListener implements View.OnClickListener {
        private Activity activity;

        OnSnackbarClickListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.action_to_nav_user_details);
            NavigationView navigationView = activity.findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_send);
        }
    }

}
