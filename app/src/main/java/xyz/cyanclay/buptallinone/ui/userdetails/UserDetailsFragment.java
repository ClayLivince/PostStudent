package xyz.cyanclay.buptallinone.ui.userdetails;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.login.LoginStatus;
import xyz.cyanclay.buptallinone.network.login.PasswordHelper;

public class UserDetailsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root = null;
    private NetworkManager nm;

    private UserDetailsViewModel mViewModel;

    private static String verifySuccess;
    private static String verifyFailed;
    private static int colorCorrect;
    private static int colorError;

    public UserDetailsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_user_details, container, false);

        nm = ((MainActivity) getActivity()).getNetworkManager();
        verifySuccess = getString(R.string.verify_success);
        verifyFailed = getString(R.string.verify_fail);
        colorCorrect = getResources().getColor(R.color.colorCorrect);
        colorError = getResources().getColor(R.color.colorError);

        final File fileDir = getContext().getFilesDir();
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
                try {
                    PasswordHelper.saveEncrypt(fileDir, ((TextView) root.findViewById(R.id.inpID)).getText().toString()
                            , ((TextView) root.findViewById(R.id.inpVPNPass)).getText().toString()
                            , ((TextView) root.findViewById(R.id.inpInfoPass)).getText().toString()
                            , ((TextView) root.findViewById(R.id.inpJwxtPass)).getText().toString()
                            , ((TextView) root.findViewById(R.id.inpJwglPass)).getText().toString());
                } catch (IOException e) {
                    Snackbar.make(root, R.string.unknown_error, Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        onRefresh();

        ((SwipeRefreshLayout) root.findViewById(R.id.srlUserDetails)).setOnRefreshListener(this);
        return root;
    }

    private static void showCaptcha(final NetworkManager nm, final View root) {
        final ImageView captchaView = root.findViewById(R.id.imageViewJwCaptcha);
        final SwipeRefreshLayout srl = root.findViewById(R.id.srlUserDetails);
        AsyncTask<Void, Void, Drawable> captchaTask = new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... voids) {
                try {
                    return nm.jwxtManager.getCapImage();
                } catch (IOException e) {
                    cancel(true);
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                captchaView.setImageDrawable(drawable);
                srl.setRefreshing(false);
            }

            @Override
            protected void onCancelled() {
                Snackbar.make(root, "验证码加载失败。请尝试下拉刷新。", Snackbar.LENGTH_LONG).show();
                srl.setRefreshing(false);
            }
        };
        captchaTask.execute();
    }

    @Override
    public void onRefresh() {
        final File fileDir = getContext().getFilesDir();
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
            String[] details = PasswordHelper.loadDecrypt(fileDir);
            if (details[0] != null) inpID.setText(details[0]);
            if (details[1] != null) inpVPN.setText(details[1]);
            if (details[2] != null) inpInfo.setText(details[2]);
            if (details[3] != null) inpJwgl.setText(details[3]);
            if (details[4] != null) inpJwxt.setText(details[4]);
        } catch (IOException e) {
            Snackbar.make(root, "在获取保存的密码时出现问题。" + e.toString(), Snackbar.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    void deterVisibility() {
        EditText inpInfo = root.findViewById(R.id.inpInfoPass);
        EditText inpJwgl = root.findViewById(R.id.inpJwglPass);
        //EditText inpJwxt = root.findViewById(R.id.inpJwxtPass);
        //EditText inpJwxtCap = root.findViewById(R.id.inpJwCaptcha);
        Button save = root.findViewById(R.id.buttonSaveUserDetail);
        EditText[] edits = new EditText[]{inpInfo, inpJwgl};
        if (nm.isSchoolNet | nm.vpnManager.isLoggedIn | isVerified((TextView) root.findViewById(R.id.textViewVPNVerify))) {
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

        String id = ((TextView) root.findViewById(R.id.inpID)).getText().toString();

        if (verifyID((TextView) root.findViewById(R.id.textViewVPNVerify), (EditText) root.findViewById(R.id.inpVPNPass))) {
            if (nm.isSchoolNet) {
                if (!isVerified((TextView) root.findViewById(R.id.textViewInfoVerify)))
                    verifyInfo(id);
                if (!isVerified((TextView) root.findViewById(R.id.textViewJwglVerify)))
                    verifyJwgl(id);
            } else {
                if (!isVerified((TextView) root.findViewById(R.id.textViewVPNVerify))) {
                    verifyVPN(id);
                    Snackbar.make(root, "非校园网先验证VPN哦~待成功后再点击一次验证按钮~", Snackbar.LENGTH_LONG).show();
                } else {
                    if (!isVerified((TextView) root.findViewById(R.id.textViewInfoVerify)))
                        verifyInfo(id);
                    if (!isVerified((TextView) root.findViewById(R.id.textViewJwglVerify)))
                        verifyJwgl(id);
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

    private void verifyVPN(String id) {
        VerifyTask.verify(this, id, root, (TextView) root.findViewById(R.id.textViewVPNVerify)
                , (EditText) root.findViewById(R.id.inpVPNPass)
                , (ProgressBar) root.findViewById(R.id.progressBarVPN)
                , nm.vpnManager);
    }

    private void verifyInfo(String id) {
        VerifyTask.verify(this, id, root, (TextView) root.findViewById(R.id.textViewInfoVerify)
                , (EditText) root.findViewById(R.id.inpInfoPass)
                , (ProgressBar) root.findViewById(R.id.progressBarInfoPass)
                , nm.infoManager);
    }

    private void verifyJwgl(String id) {
        VerifyTask.verify(this, id, root, (TextView) root.findViewById(R.id.textViewJwglVerify)
                , (EditText) root.findViewById(R.id.inpJwglPass)
                , (ProgressBar) root.findViewById(R.id.progressBarJwglPass)
                , nm.jwglManager);
    }

    private static void verifyJwxt(final View root, final NetworkManager nm, String id) {
        final EditText inpJwxtPass = root.findViewById(R.id.inpJwxtPass);
        final EditText inpJwCaptcha = root.findViewById(R.id.inpJwCaptcha);
        String jwxtPass = inpJwxtPass.getText().toString();
        String captcha = inpJwCaptcha.getText().toString();
        final ProgressBar jwxtProgress = root.findViewById(R.id.progressBarJwxtPass);
        final ProgressBar captchaProgress = root.findViewById(R.id.progressBarJwCaptcha);

        final TextView jwxtStat = root.findViewById(R.id.textViewJwxtVerify);
        final TextView captchaStat = root.findViewById(R.id.textViewCaptchaVerify);
        if (jwxtPass.length() != 0) {
            if (captcha.length() == 4) {
                jwxtStat.setVisibility(View.VISIBLE);
                nm.jwxtManager.setDetails(id, jwxtPass);
                captchaStat.setVisibility(View.VISIBLE);
                nm.jwxtManager.setJwCap(captcha);
                jwxtProgress.setVisibility(View.VISIBLE);
                captchaProgress.setVisibility(View.VISIBLE);

                new AsyncTask<Void, Void, LoginStatus>() {
                    @Override
                    protected LoginStatus doInBackground(Void... voids) {
                        try {
                            return nm.jwxtManager.login();
                        } catch (IOException e) {
                            cancel(true);
                            e.printStackTrace();
                            if (e instanceof SocketTimeoutException) return LoginStatus.TIMED_OUT;
                        }
                        return LoginStatus.UNKNOWN_ERROR;
                    }

                    @Override
                    protected void onPostExecute(LoginStatus loginStatus) {
                        super.onPostExecute(loginStatus);
                        jwxtProgress.setVisibility(View.INVISIBLE);
                        captchaProgress.setVisibility(View.INVISIBLE);
                        switch (loginStatus) {
                            case LOGIN_SUCCESS: {
                                verifySuccess(jwxtStat);
                                verifySuccess(captchaStat);
                                break;
                            }
                            case INCORRECT_DETAIL: {
                                verifyFailed(jwxtStat, inpJwxtPass);
                                Snackbar.make(root, R.string.incorrect_jwxt_password, Snackbar.LENGTH_LONG).show();
                                break;
                            }
                            case INCORRECT_CAPTCHA: {
                                verifyFailed(captchaStat, inpJwCaptcha);
                                Snackbar.make(root, R.string.incorrect_captcha, Snackbar.LENGTH_LONG).show();
                                break;
                            }
                            case TIMED_OUT: {
                                verifyFailed(jwxtStat, inpJwxtPass);
                                Snackbar.make(root, R.string.load_failed, Snackbar.LENGTH_LONG).show();
                                break;
                            }
                            default: {
                                Snackbar.make(root, R.string.unknown_error, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                }.execute();
            } else {
                verifyFailed(captchaStat, inpJwCaptcha);
                Snackbar.make(root, R.string.input_captcha, Snackbar.LENGTH_LONG).show();
            }
        }

        /*
        AlertDialog.Builder captchaDialogBuilder = new AlertDialog.Builder(getContext());
            final View captchaView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_captcha_input_panel, null);
            captchaDialogBuilder.setView(captchaView);
            captchaDialogBuilder.setTitle(R.string.input_captcha);
            ImageView iv = captchaView.findViewById(R.id.imageViewCaptcha);
            iv.setImageDrawable(nm.jwxtManager.getCapImage());
            captchaDialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TextView tv = captchaView.findViewById(R.id.inpCaptcha);
                    String captcha = tv.getText().toString();
                    if (captcha.length() == 4) {
                        nm.jwxtManager.setJwCap(captcha);
                        if (nm.jwxtManager.jwLogin().equals("已成功登录")){

                        }
                    }
                }
            });
         */
    }

    private static synchronized void verifyFailed(TextView verify, EditText inp) {
        verify.setText(verifyFailed);
        verify.setTextColor(colorError);
        verify.setVisibility(View.VISIBLE);
        inp.setFocusableInTouchMode(true);
        inp.requestFocus();
    }

    private static synchronized void verifySuccess(TextView verify) {
        verify.setText(verifySuccess);
        verify.setTextColor(colorCorrect);
        verify.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        deterVisibility();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(UserDetailsFragment.this.root.getWindowToken(), 0);
                        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UserDetailsViewModel.class);
        // TODO: Use the ViewModel
    }


}
