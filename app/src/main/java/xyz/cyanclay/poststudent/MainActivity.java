package xyz.cyanclay.poststudent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;

import xyz.cyanclay.poststudent.network.AuthManager;
import xyz.cyanclay.poststudent.network.JwxtManager;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.network.SiteManager;
import xyz.cyanclay.poststudent.network.VPNManager;
import xyz.cyanclay.poststudent.network.info.InfoManager;
import xyz.cyanclay.poststudent.network.login.LoginTask;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;
import xyz.cyanclay.poststudent.ui.userdetails.UserDetailsFragment;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NetworkManager networkManager;
    private UserDetailsFragment userDetailsFragment = new UserDetailsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final NavigationView navigationView = findViewById(R.id.nav_view);

        try {
            networkManager = new NetworkManager(getApplicationContext());
        } catch (final IOException e) {
            e.printStackTrace();
            Snackbar.make(navigationView, R.string.init_failed, Snackbar.LENGTH_LONG)
                    .setAction(e.getMessage(), null).show();
        }

        if (networkManager != null) {
            taskCheckUpdate(MainActivity.this, navigationView);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("厚德博学 敬业乐群");
        toolbar.setSubtitleTextColor(0xffffffff);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_info, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    public void setUser(String name) {
        NavigationView nv = findViewById(R.id.nav_view);
        ((TextView) nv.getHeaderView(0).findViewById(R.id.textViewNavName)).setText(getString(R.string.welcome, name));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void onUserDetailsClick(View v) {
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_to_nav_user_details);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_send);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
    }

    public void popupCaptcha(final View from, Drawable image, SiteManager who) {
        AlertDialog.Builder captchaDialogBuilder = new AlertDialog.Builder(this);
        final View captchaView = LayoutInflater.from(this).inflate(R.layout.dialog_captcha_input_panel, null);
        captchaDialogBuilder.setTitle(R.string.input_captcha);

        String message;
        if (who instanceof VPNManager) {
            message = getString(R.string.vpn_captcha);
        } else if (who instanceof AuthManager) {
            who = who.nm.infoManager;
            message = getString(R.string.info_captcha);
        } else if (who instanceof InfoManager) {
            message = getString(R.string.info_captcha);
        } else if (who instanceof JwxtManager) {
            message = getString(R.string.jwxt_captcha);
        } else message = getString(R.string.captcha);
        TextView tv = captchaView.findViewById(R.id.textViewDialogCaptcha);
        tv.setText(message);

        ImageView iv = captchaView.findViewById(R.id.imageViewCaptcha);
        final EditText et = captchaView.findViewById(R.id.inpCaptcha);
        iv.setImageDrawable(image);

        final SiteManager whoF = who;
        captchaDialogBuilder.setView(captchaView);
        captchaDialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginTask.login(MainActivity.this, from, whoF, et.getText().toString());
                dialog.dismiss();
            }
        });

        captchaDialogBuilder.show();
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    private static void taskCheckUpdate(final MainActivity activity, final View view) {
        final String[] msg = new String[1];
        new TryAsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return activity.networkManager.updateManager.checkForUpdates();
                } catch (JSONException e) {
                    e.printStackTrace();
                    cancel(true);
                    msg[0] = (String) activity.getResources().getText(R.string.update_problem);
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel(true);
                    msg[0] = "网络不给力！";
                }
                return false;
            }

            @Override
            protected void postExecute(Boolean result) {
                if (result) {
                    taskUpdateConfirm(activity, view);
                }
            }

            @Override
            protected void cancelled(Boolean result) throws Exception {
                Snackbar.make(view, msg[0], Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }

    private static void taskUpdateConfirm(final MainActivity activity, final View view) {
        final String[] msg = new String[1];
        new TryAsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... voids) {
                try {
                    String[] infos = activity.networkManager.updateManager.getUpdateInfo();
                    if (infos.length < 3) throw new IOException();
                    return infos;
                } catch (JSONException e) {
                    e.printStackTrace();
                    cancel(true);
                    msg[0] = "发生问题！";
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel(true);
                    msg[0] = "网络不给力！";
                }
                return null;
            }

            @Override
            protected void postExecute(String[] infos) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle(infos[1]);
                dialogBuilder.setMessage("Version :" + infos[0] + "\n" + infos[2]);
                dialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.networkManager.updateManager.update();
                        Snackbar.make(view, R.string.start_update, BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialogBuilder.setCancelable(true);
                dialogBuilder.show();
            }

            @Override
            protected void cancelled(String[] result) {
                Snackbar.make(view, msg[0], Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }
}