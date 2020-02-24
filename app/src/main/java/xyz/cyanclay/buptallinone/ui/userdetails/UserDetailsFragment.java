package xyz.cyanclay.buptallinone.ui.userdetails;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.InfoManager;
import xyz.cyanclay.buptallinone.network.LoginStatus;
import xyz.cyanclay.buptallinone.network.NetworkManager;

public class UserDetailsFragment extends Fragment {

    private View root = null;
    private UserDetailsViewModel mViewModel;

    public static UserDetailsFragment newInstance() {
        return new UserDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_user_details, container, false);

        final NetworkManager nm = ((MainActivity) getActivity()).getNetworkManager();
        ((ImageView) root.findViewById(R.id.imageViewJwCaptcha)).setImageDrawable(nm.jwxtManager.getCapImage());

        root.findViewById(R.id.buttonVerifyPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inpID = root.findViewById(R.id.inpID);
                String id = inpID.getText().toString();

                TextView idStat = root.findViewById(R.id.textViewIDVerify);
                TextView infoStat = root.findViewById(R.id.textViewInfoVerify);
                TextView jwxtStat = root.findViewById(R.id.textViewJwxtVerify);
                TextView jwglStat = root.findViewById(R.id.textViewJwglVerify);

                idStat.setVisibility(View.VISIBLE);
                infoStat.setVisibility(View.VISIBLE);
                jwxtStat.setVisibility(View.VISIBLE);
                jwglStat.setVisibility(View.VISIBLE);

                if (id.length() == 10) {
                    EditText inpInfoPass = root.findViewById(R.id.inpInfoPassword);
                    String infoPass = inpInfoPass.getText().toString();
                    if (infoPass.length() != 0) {

                        nm.infoManager.setLoginDetails(id, infoPass);
                        if (nm.infoManager.infoLogin() == LoginStatus.LOGIN_SUCCESS) {
                            verifySuccess(idStat);
                            verifySuccess(infoStat);
                            activateButtonSave();

                            verifyJwxt(nm, id);
                            verifyJwgl(nm, id);
                        } else {
                            verifyFailed(infoStat, inpInfoPass);
                            Snackbar.make(root, getString(R.string.incorrect_info_password), Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        verifyFailed(infoStat, inpInfoPass);
                        Snackbar.make(root, getString(R.string.please_input_info), Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    verifyFailed(idStat, inpID);
                    Snackbar.make(root, getString(R.string.incorrect_id), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }


    private void verifyFailed(TextView verify, EditText inp){
        verify.setText(getString(R.string.verify_fail));
        verify.setTextColor(getResources().getColor(R.color.colorError));
        verify.setVisibility(View.VISIBLE);
        inp.setFocusableInTouchMode(true);
        inp.requestFocus();
    }

    private void verifySuccess(TextView verify){
        verify.setText(getString(R.string.verify_success));
        verify.setTextColor(getResources().getColor(R.color.colorCorrect));
        verify.setVisibility(View.VISIBLE);
    }

    private void verifyJwxt(final NetworkManager nm, String id){
        EditText inpJwxtPass = root.findViewById(R.id.inpJwxtPassword);
        EditText inpJwCaptcha = root.findViewById(R.id.inpJwCaptcha);
        String jwxtPass = inpJwxtPass.getText().toString();
        String captcha = inpJwCaptcha.getText().toString();
        TextView jwxtStat = root.findViewById(R.id.textViewJwxtVerify);
        TextView captchaStat = root.findViewById(R.id.textViewCaptchaVerify);
        if (jwxtPass.length() != 0){
            jwxtStat.setVisibility(View.VISIBLE);
            nm.jwxtManager.setJwDetails(id, jwxtPass);
            if (captcha.length() == 4){
                captchaStat.setVisibility(View.VISIBLE);
                nm.jwxtManager.setJwCap(captcha);
                LoginStatus status = nm.jwxtManager.jwLogin();
                switch (status){
                    case LOGIN_SUCCESS:{
                        verifySuccess(jwxtStat);
                        verifySuccess(captchaStat);
                        break;
                    }
                    case INCORRECT_DETAIL:{
                        verifyFailed(jwxtStat, inpJwxtPass);
                        Snackbar.make(root, getString(R.string.incorrect_jwxt_password), Snackbar.LENGTH_LONG).show();
                        break;
                    }
                    case INCORRECT_CAPTCHA:{
                        verifyFailed(captchaStat, inpJwCaptcha);
                        Snackbar.make(root, getString(R.string.incorrect_captcha), Snackbar.LENGTH_LONG).show();
                        break;
                    }
                }
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

    private void activateButtonSave(){
        Button btnSave = root.findViewById(R.id.buttonSaveUserDetail);
        btnSave.setVisibility(View.VISIBLE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(UserDetailsFragment.this.root.getWindowToken(), 0);
                    // ((MainActivity) getActivity()).setHomeFragment();
                    return true;
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
