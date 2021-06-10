package xyz.cyanclay.poststudent.ui.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.UpdateManager;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;

public class SettingFragment extends Fragment {

    private View root;
    private MainActivity activity;
    private UpdateManager updateManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
        updateManager = activity.getNetworkManager().updateManager;
        view.findViewById(R.id.setting_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(activity, R.id.nav_host_fragment)
                        .navigate(R.id.action_nav_slideshow_to_aboutFragment);
            }
        });

        view.findViewById(R.id.setting_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskCheckUpdate(SettingFragment.this);
            }
        });
    }

    private static void taskCheckUpdate(final SettingFragment fragment) {
        final String[] msg = new String[1];
        new TryAsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return fragment.updateManager.checkForUpdates();
                } catch (JSONException e) {
                    e.printStackTrace();
                    cancel(true);
                    msg[0] = "发生问题！";
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
                    taskUpdateConfirm(fragment);
                } else {
                    Snackbar.make(fragment.root, R.string.is_latest, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void cancelled(Boolean result) throws Exception {
                Snackbar.make(fragment.root, msg[0], Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }

    private static void taskUpdateConfirm(final SettingFragment fragment) {
        final String[] msg = new String[1];
        new TryAsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... voids) {
                try {
                    String[] infos = fragment.updateManager.getUpdateInfo();
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
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(fragment.getContext());
                dialogBuilder.setTitle(infos[1]);
                dialogBuilder.setMessage("Version :" + infos[0] + "\n" + infos[2]);
                dialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.updateManager.update();
                        Snackbar.make(fragment.root, R.string.start_update, BaseTransientBottomBar.LENGTH_LONG).show();
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
            protected void cancelled(String[] infos) {
                Snackbar.make(fragment.root, msg[0], Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }
}