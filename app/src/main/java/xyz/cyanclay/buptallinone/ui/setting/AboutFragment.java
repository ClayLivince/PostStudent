package xyz.cyanclay.buptallinone.ui.setting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import xyz.cyanclay.buptallinone.R;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_about, container, false);

        TextView version = root.findViewById(R.id.textViewVersion);

        try {
            PackageInfo packInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String currentVersion = packInfo.versionName;
            version.setText(currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return root;
    }
}
