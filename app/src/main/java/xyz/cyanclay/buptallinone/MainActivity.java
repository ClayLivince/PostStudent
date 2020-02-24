package xyz.cyanclay.buptallinone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;

import xyz.cyanclay.buptallinone.network.NetworkManager;
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
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send, R.id.nav_user_details)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        networkManager = new NetworkManager(getApplicationContext());


        /*
        new Thread(new Runnable(){
            @Override
            public void run() {
                jwxtManager.init();
                getcap();
            }
        }).start();

         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void replaceFragment(Fragment to, int fgmId) {
        FragmentManager manager = this.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        for (Fragment fragment : manager.getFragments()) {
            transaction.hide(fragment);
        }
        if (!to.isAdded()) {
            transaction.add(fgmId, to).commit();
        } else {
            transaction.show(to).commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
    }

    public void onUserDetailsClick(View v){
        //replaceFragment();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, new UserDetailsFragment()).commit();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_send);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
    }

    public NetworkManager getNetworkManager(){
        return networkManager;
    }



    /*public void getcap(){
        final ImageView iv = findViewById(R.id.imgCap2);
        new Thread(new Runnable(){
            @Override
            public void run() {
                final Drawable dw = jwxtManager.getCapImage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iv.setImageDrawable(dw);
                    }
                });
            }
        }).start();
    }*/

    /*
    public void onVPNLogin(View v){

        new Thread(new Runnable() {
            @Override
            public void run() {
                jwxtManager.checkScore();
            }
        }).start();

     */
        /*
        final TextView editUser = findViewById(R.id.inpUser2);
        final TextView editPass = findViewById(R.id.inpPass2);
        final TextView editCap = findViewById(R.id.inpCap2);
        final TextView editVPN = findViewById(R.id.inpVPNPass);
        jwxtManager.setJwDetails(editUser.getText().toString(),
                editPass.getText().toString(),
                editCap.getText().toString());
        vpnManager.setVpnDetails(editUser.getText().toString(), editVPN.getText().toString());
        new Thread(new Runnable(){
            @Override
            public void run() {
                vpnManager.refreshVPNCookie();
            }
        }).start();
        getcap();
    }

         */

    /*
    public void onJwLogin(View v){
        getcap();
        final TextView editUser = findViewById(R.id.inpUser2);
        final TextView editPass = findViewById(R.id.inpPass2);
        final TextView editCap = findViewById(R.id.inpCap2);
        final TextView editVPN = findViewById(R.id.inpVPNPass);
        final TextView status = findViewById(R.id.status2);
        jwxtManager.setJwDetails(editUser.getText().toString(),
                editPass.getText().toString(),
                editCap.getText().toString());
        vpnManager.setVpnDetails(editUser.getText().toString(), editVPN.getText().toString());
        new Thread(new Runnable(){
            @Override
            public void run() {
                final String result = jwxtManager.jwLogin();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(result);
                    }
                });
            }
        }).start();

    }

     */
}
