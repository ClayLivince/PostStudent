package xyz.cyanclay.poststudent.ui.userdetails;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.AuthManager;
import xyz.cyanclay.poststudent.network.JwxtManager;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.network.SiteManager;
import xyz.cyanclay.poststudent.network.VPNManager;
import xyz.cyanclay.poststudent.network.info.InfoManager;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;

import static xyz.cyanclay.poststudent.ui.userdetails.VerifyTask.verifyFailed;

public class UserDetailsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    View root = null;
    private NetworkManager nm;
    String id = "";

    private static String verifySuccess;
    private static Logger logger = LogManager.getLogger(UserDetailsFragment.class);

    private ProgressDialog saveDialog;

    public UserDetailsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_user_details, container, false);

        nm = ((MainActivity) requireActivity()).getNetworkManager();
        verifySuccess = getString(R.string.verify_success);

        final File fileDir = requireContext().getFilesDir();
        loadDetails(root, fileDir);
        //showCaptcha(nm, root);
        root.findViewById(R.id.buttonVerifyPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVerify();
            }
        });
        root.findViewById(R.id.buttonSaveUserDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog = new ProgressDialog(UserDetailsFragment.this.getContext());
                saveDialog.setMessage("正在保存...");
                saveDialog.setCancelable(false);//默认true
                saveDialog.setCanceledOnTouchOutside(false);//默认true
                saveDialog.show();
                taskSavePassword(UserDetailsFragment.this, fileDir
                        , ((TextView) root.findViewById(R.id.inpID)).getText().toString()
                        , ((TextView) root.findViewById(R.id.inpVPNPass)).getText().toString()
                        , ((TextView) root.findViewById(R.id.inpInfoPass)).getText().toString()
                        , ((TextView) root.findViewById(R.id.inpJwglPass)).getText().toString()
                        , ((TextView) root.findViewById(R.id.inpJwxtPass)).getText().toString());
            }
        });
        onRefresh();

        ((SwipeRefreshLayout) root.findViewById(R.id.srlUserDetails)).setOnRefreshListener(this);
        return root;
    }

    @Override
    public void onRefresh() {
        final File fileDir = requireContext().getFilesDir();
        loadDetails(root, fileDir);
        //showCaptcha(nm, root);
        deterVisibility();
    }

    private void loadDetails(View root, File fileDir) {

        EditText inpID = root.findViewById(R.id.inpID);
        EditText inpVPN = root.findViewById(R.id.inpVPNPass);
        EditText inpInfo = root.findViewById(R.id.inpInfoPass);
        EditText inpJwgl = root.findViewById(R.id.inpJwglPass);
        EditText inpJwxt = root.findViewById(R.id.inpJwxtPass);

        try {
            String[] details = nm.passwordHelper.loadDecrypt(fileDir);
            if (details[0] != null) inpID.setText(details[0]);
            if (details[1] != null) inpVPN.setText(details[1]);
            if (details[2] != null) inpInfo.setText(details[2]);
            if (details[3] != null) inpJwgl.setText(details[3]);
            if (details[4] != null) inpJwxt.setText(details[4]);
        } catch (IOException e) {
            Snackbar.make(root, "在获取保存的密码时出现问题。" + e.toString(), Snackbar.LENGTH_LONG);
            e.printStackTrace();
        }

        ((SwipeRefreshLayout) root.findViewById(R.id.srlUserDetails)).setRefreshing(false);
    }

    void deterVisibility() {
        EditText inpInfo = root.findViewById(R.id.inpInfoPass);
        EditText inpJwgl = root.findViewById(R.id.inpJwglPass);
        //EditText inpJwxt = root.findViewById(R.id.inpJwxtPass);
        //EditText inpJwxtCap = root.findViewById(R.id.inpJwCaptcha);
        Button save = root.findViewById(R.id.buttonSaveUserDetail);
        EditText[] edits = new EditText[]{inpInfo, inpJwgl};
        if (nm.isSchoolNet | nm.vpnManager.isLoggedIn() | isVerified((TextView) root.findViewById(R.id.textViewVPNVerify))) {
            for (EditText edit : edits)
                edit.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
        } else {
            for (EditText edit : edits)
                edit.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
        }
    }

    private void startVerify() {
        SwipeRefreshLayout srl = root.findViewById(R.id.srlUserDetails);
        srl.setEnabled(false);

        id = ((TextView) root.findViewById(R.id.inpID)).getText().toString();

        if (verifyID((TextView) root.findViewById(R.id.textViewVPNVerify), (EditText) root.findViewById(R.id.inpVPNPass))) {
            if (nm.isSchoolNet) {
                if (!isVerified((TextView) root.findViewById(R.id.textViewInfoVerify)))
                    verifyInfo();
                if (!isVerified((TextView) root.findViewById(R.id.textViewJwglVerify)))
                    verifyJwgl();
            } else {
                if (!isVerified((TextView) root.findViewById(R.id.textViewVPNVerify))) {
                    verifyVPN();
                    Snackbar.make(root, "非校园网先验证VPN哦~待成功后再点击一次验证按钮~", Snackbar.LENGTH_LONG).show();
                } else {
                    if (!isVerified((TextView) root.findViewById(R.id.textViewInfoVerify)))
                        verifyInfo();
                    if (!isVerified((TextView) root.findViewById(R.id.textViewJwglVerify)))
                        verifyJwgl();
                    //if (!isVerified((TextView) root.findViewById(R.id.textViewJwxtVerify)))
                    //verifyJwxt(root, nm, id);
                }
            }
        }
        deterVisibility();

        srl.setEnabled(true);
    }

    private boolean isVerified(TextView view) {
        return view.getText().toString().equals(verifySuccess);
    }

    private boolean verifyID(TextView stat, EditText input) {
        EditText inpID = root.findViewById(R.id.inpID);
        String id = inpID.getText().toString();

        TextView idStat = root.findViewById(R.id.textViewIDVerify);

        idStat.setVisibility(View.VISIBLE);
        stat.setVisibility(View.VISIBLE);

        if (id.length() == 10) {
            idStat.setVisibility(View.INVISIBLE);
            String pass = input.getText().toString();
            if (pass.length() != 0) return true;
            else {
                verifyFailed(stat, input);
                Snackbar.make(root, R.string.please_input_info, Snackbar.LENGTH_LONG).show();
            }
        } else {
            verifyFailed(idStat, inpID);
            Snackbar.make(root, R.string.incorrect_id, Snackbar.LENGTH_LONG).show();
        }
        return false;
    }

    private void verifyVPN() {
        VerifyTask.verify(this, nm.vpnManager);
    }

    private void verifyInfo() {
        VerifyTask.verify(this, nm.infoManager);
    }

    private void verifyJwgl() {
        VerifyTask.verify(this, nm.jwglManager);
    }

    /*
    private void verifyJwxt() {
        VerifyTask.verify(this, nm.jwxtManager);
    }
     */

    void popupCaptcha(Drawable image, SiteManager who) {
        AlertDialog.Builder captchaDialogBuilder = new AlertDialog.Builder(getContext());
        final View captchaView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_captcha_input_panel, null);
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

        captchaDialogBuilder.setView(captchaView);

        final SiteManager whoF = who;
        captchaDialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VerifyTask.verify(UserDetailsFragment.this, whoF, et.getText().toString());
                dialog.dismiss();
            }
        });

        captchaDialogBuilder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        deterVisibility();
        View view = requireView();
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP) {
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null)
                            imm.hideSoftInputFromWindow(UserDetailsFragment.this.root.getWindowToken(), 0);
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    private static void taskSavePassword(final UserDetailsFragment udf,
                                         final File fileDir, final String... details) {
        new TryAsyncTask<Void, Void, Boolean>() {
            Exception e;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    udf.nm.passwordHelper.saveEncrypt(fileDir, details[0]
                            , details[1], details[2], details[3], details[4]);
                    Thread.sleep(500);
                    String[] decrypted = udf.nm.passwordHelper.loadDecrypt(fileDir);
                    logger.info("Original: " + Arrays.toString(details));
                    logger.info("Decrypted: " + Arrays.toString(decrypted));
                    return Arrays.equals(decrypted, details);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    this.e = e;
                    cancel(true);
                    return false;
                }
            }

            @Override
            protected void postExecute(Boolean aBoolean) {
                udf.saveDialog.dismiss();
                if (aBoolean)
                    Snackbar.make(udf.root, "保存成功！", Snackbar.LENGTH_LONG).show();
                else Snackbar.make(udf.root, "保存失败，请重试", Snackbar.LENGTH_LONG).show();
            }

            @Override
            protected void cancelled(Boolean a) {
                udf.saveDialog.dismiss();
                Snackbar.make(udf.root, "保存失败，请重试", Snackbar.LENGTH_LONG).show();
                if (this.e != null) logger.error("Exception in Saving Password: ", e);
            }
        }.execute();
    }

}
