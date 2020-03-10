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

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.LoginStatus;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.PasswordHelper;

public class UserDetailsFragment extends Fragment {

    private View root = null;
    private UserDetailsViewModel mViewModel;

    private static String verifySuccess;
    private static String verifyFailed;
    private static int colorCorrect;
    private static int colorError;

    public static UserDetailsFragment newInstance() {
        return new UserDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_user_details, container, false);

        final NetworkManager nm = ((MainActivity) getActivity()).getNetworkManager();
        final ImageView captchaView = root.findViewById(R.id.imageViewJwCaptcha);

        showCaptcha(nm, captchaView);
        verifySuccess = getString(R.string.verify_success);
        verifyFailed = getString(R.string.verify_fail);
        colorCorrect = getResources().getColor(R.color.colorCorrect);
        colorError = getResources().getColor(R.color.colorError);

        final File fileDir = getContext().getFilesDir();

        root.findViewById(R.id.buttonVerifyPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inpID = root.findViewById(R.id.inpID);
                String id = inpID.getText().toString();

                TextView idStat = root.findViewById(R.id.textViewIDVerify);
                TextView infoStat = root.findViewById(R.id.textViewInfoVerify);

                idStat.setVisibility(View.VISIBLE);
                infoStat.setVisibility(View.VISIBLE);

                if (id.length() == 10) {
                    EditText inpInfoPass = root.findViewById(R.id.inpInfoPassword);
                    String infoPass = inpInfoPass.getText().toString();
                    if (infoPass.length() != 0) {
                        verifyInfo(root, getContext(), nm, infoStat, idStat, inpID, inpInfoPass, id, fileDir);
                    } else {
                        verifyFailed(infoStat, inpInfoPass);
                        Snackbar.make(root, R.string.please_input_info, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    verifyFailed(idStat, inpID);
                    Snackbar.make(root, R.string.incorrect_id, Snackbar.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }

    private static void showCaptcha(final NetworkManager nm, final ImageView view) {
        AsyncTask<Void, Void, Drawable> captchaTask = new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... voids) {
                return nm.jwxtManager.getCapImage();
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                view.setImageDrawable(drawable);
            }
        };
        captchaTask.execute();
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

    private static void verifyInfo(final View root, final Context context, final NetworkManager nm, final TextView infoStat, final TextView idStat,
                                   final EditText inpID, final EditText inpInfoPass, final String id, final File fileDir) {
        nm.infoManager.setLoginDetails(id, inpInfoPass.getText().toString());
        infoStat.setVisibility(View.VISIBLE);

        final ProgressBar idProgress = root.findViewById(R.id.progressBarID);
        final ProgressBar infoProgress = root.findViewById(R.id.progressBarInfoPass);
        idProgress.setVisibility(View.VISIBLE);
        infoProgress.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, LoginStatus>() {
            @Override
            protected LoginStatus doInBackground(Void... voids) {
                try {
                    return nm.infoManager.infoLogin();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return LoginStatus.UNKNOWN_ERROR;
            }

            @Override
            protected void onPostExecute(LoginStatus loginStatus) {
                super.onPostExecute(loginStatus);
                idProgress.setVisibility(View.INVISIBLE);
                infoProgress.setVisibility(View.INVISIBLE);
                switch (loginStatus) {
                    case LOGIN_SUCCESS: {
                        verifySuccess(idStat);
                        verifySuccess(infoStat);
                        activateButtonSave(root, context, fileDir);
                        verifyJwxt(root, nm, id);
                        break;
                    }
                    case INCORRECT_DETAIL: {
                        verifyFailed(idStat, inpID);
                        verifyFailed(infoStat, inpInfoPass);
                        Snackbar.make(root, R.string.incorrect_info_password, Snackbar.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        verifyFailed(idStat, inpID);
                        verifyFailed(infoStat, inpInfoPass);
                        Snackbar.make(root, R.string.unknown_error, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        }.execute();
    }

    private static void verifyJwxt(final View root, final NetworkManager nm, String id) {
        final EditText inpJwxtPass = root.findViewById(R.id.inpJwxtPassword);
        final EditText inpJwCaptcha = root.findViewById(R.id.inpJwCaptcha);
        String jwxtPass = inpJwxtPass.getText().toString();
        String captcha = inpJwCaptcha.getText().toString();
        final ProgressBar jwxtProgress = root.findViewById(R.id.progressBarJwxtPass);
        final ProgressBar captchaProgress = root.findViewById(R.id.progressBarJwCaptcha);

        final TextView jwxtStat = root.findViewById(R.id.textViewJwxtVerify);
        final TextView captchaStat = root.findViewById(R.id.textViewCaptchaVerify);
        if (jwxtPass.length() != 0){
            if (captcha.length() == 4) {
                jwxtStat.setVisibility(View.VISIBLE);
                nm.jwxtManager.setJwDetails(id, jwxtPass);
                captchaStat.setVisibility(View.VISIBLE);
                nm.jwxtManager.setJwCap(captcha);
                jwxtProgress.setVisibility(View.VISIBLE);
                captchaProgress.setVisibility(View.VISIBLE);

                new AsyncTask<Void, Void, LoginStatus>() {
                    @Override
                    protected LoginStatus doInBackground(Void... voids) {
                        return nm.jwxtManager.jwLogin();
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

    private void verifyJwgl(NetworkManager nm, String id){

    }

    private static void activateButtonSave(final View root, final Context context, final File fileDir) {
        Button btnSave = root.findViewById(R.id.buttonSaveUserDetail);
        btnSave.setVisibility(View.VISIBLE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PasswordHelper.saveEncrypt(fileDir, ((TextView) root.findViewById(R.id.inpID)).getText().toString()
                            , ((TextView) root.findViewById(R.id.inpInfoPassword)).getText().toString()
                            , ((TextView) root.findViewById(R.id.inpJwxtPassword)).getText().toString()
                            , ((TextView) root.findViewById(R.id.inpJwglPassword)).getText().toString());
                } catch (IOException e) {
                    Snackbar.make(root, R.string.unknown_error, Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
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
                        // ((MainActivity) getActivity()).setHomeFragment();
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
