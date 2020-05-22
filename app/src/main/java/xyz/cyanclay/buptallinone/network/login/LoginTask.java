package xyz.cyanclay.buptallinone.network.login;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.navigation.Navigation;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.SocketTimeoutException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.SiteManager;

public class LoginTask {

    public static void login(final Activity activity, final View root,
                      final SiteManager site, final String captcha){
        new AsyncTask<Void, Void, LoginStatus>(){
            LoginException exception = null;
            @Override
            protected LoginStatus doInBackground(Void... voids) {
                try{
                    return site.login(captcha);
                } catch (IOException e){
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) return LoginStatus.TIMED_OUT;
                    else cancel(true);
                } catch (LoginException e){
                    exception = e;
                    cancel(true);
                }
                return LoginStatus.UNKNOWN_ERROR;
            }

            @Override
            protected void onPostExecute(LoginStatus status) {
                super.onPostExecute(status);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                if (exception != null){
                    handleStatus(activity, root, exception.status);
                }
            }

        }.execute();
    }

    public static void handleStatus(Activity activity, View root, LoginStatus status){
        View.OnClickListener listener = new OnSnackbarClickListener(activity);
        switch (status){
            case EMPTY_USERNAME:
            case EMPTY_PASSWORD:{
                makeSnackbar(root, R.string.empty_details)
                        .setAction(R.string.go_fill_details, listener).show();
                break;
            }
            case INCORRECT_DETAIL:{
                makeSnackbar(root, R.string.incorrect_details)
                        .setAction(R.string.go_fill_details, listener).show();
                break;
            }
            case INCORRECT_CAPTCHA: {
                makeSnackbar(root, R.string.incorrect_captcha).show();
                break;
            }
            case CAPTCHA_REQUIRED:
            case EMPTY_CAPTCHA:{
                makeSnackbar(root, R.string.incorrect_captcha).show();
                ((MainActivity) activity).popupCaptcha(root, status.captchaImage, status.site);
                break;
            }
            case TOO_MANY_ERRORS:{
                makeSnackbar(root, R.string.vpn_too_many_errors).show();
                break;
            }
            case TIMED_OUT:{
                makeSnackbar(root, R.string.timed_out).show();
                break;
            }
            case UNKNOWN_ERROR: {
                makeSnackbar(root, R.string.unknown_error).show();
                break;
            }
        }
    }

    private static Snackbar makeSnackbar(View root, @StringRes int id){
        try{
            return Snackbar.make(root, id, Snackbar.LENGTH_LONG);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            return Snackbar.make(root, "由于切换页面，原本内容未能正常显示。", Snackbar.LENGTH_LONG);
        }
    }

    private static class OnSnackbarClickListener implements View.OnClickListener{
        private Activity activity;
        OnSnackbarClickListener(Activity activity){
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
