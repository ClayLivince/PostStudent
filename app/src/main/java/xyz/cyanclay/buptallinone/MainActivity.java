package xyz.cyanclay.buptallinone;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import xyz.cyanclay.buptallinone.network.JwxtManager;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.SiteManager;
import xyz.cyanclay.buptallinone.network.VPNManager;
import xyz.cyanclay.buptallinone.network.info.InfoManager;
import xyz.cyanclay.buptallinone.network.login.LoginTask;
import xyz.cyanclay.buptallinone.ui.userdetails.UserDetailsFragment;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NetworkManager networkManager;
    private UserDetailsFragment userDetailsFragment = new UserDetailsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("厚德博学 敬业乐群");
        toolbar.setSubtitleTextColor(0xffffffff);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_info, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    networkManager = new NetworkManager(getApplicationContext());
                } catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(navigationView, R.string.unknown_error, Snackbar.LENGTH_LONG)
                                    .setAction(e.getMessage(), null).show();
                        }
                    });
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setUser(networkManager.user, networkManager.name);
                    }
                });
            }
        }.start();
    }

    public void setUser(String user, String name) {
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

    public void popupCaptcha(final View from, Drawable image, final SiteManager who){
        AlertDialog.Builder captchaDialogBuilder = new AlertDialog.Builder(this);
        final View captchaView = LayoutInflater.from(this).inflate(R.layout.dialog_captcha_input_panel, null);
        captchaDialogBuilder.setTitle(R.string.input_captcha);

        String message;
        if (who instanceof VPNManager){
            message = getString(R.string.vpn_captcha);
        } else if (who instanceof InfoManager){
            message = getString(R.string.info_captcha);
        } else if (who instanceof JwxtManager){
            message = getString(R.string.jwxt_captcha);
        } else message = getString(R.string.captcha);
        TextView tv = captchaView.findViewById(R.id.textViewDialogCaptcha);
        tv.setText(message);

        ImageView iv = captchaView.findViewById(R.id.imageViewCaptcha);
        final EditText et = captchaView.findViewById(R.id.inpCaptcha);
        iv.setImageDrawable(image);

        captchaDialogBuilder.setView(captchaView);
        captchaDialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginTask.login(MainActivity.this, from, who, et.getText().toString());
                dialog.dismiss();
            }
        });

        captchaDialogBuilder.show();
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }
}