package xyz.cyanclay.buptallinone.ui.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import xyz.cyanclay.buptallinone.R;

public class ToolsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ToolsViewModel toolsViewModel = ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tools, container, false);


        root.findViewById(R.id.imageViewTrainMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoTrainMode();
            }
        });
        root.findViewById(R.id.imageViewSchoolBus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSchoolBus();
            }
        });
        return root;
    }

    void gotoTrainMode() {
        Navigation.findNavController(this.requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_to_nav_train_mode);
        //navigationView.setCheckedItem(R.id.nav_send);
    }

    void gotoSchoolBus() {
        Navigation.findNavController(this.requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_to_nav_school_bus);
        //navigationView.setCheckedItem(R.id.nav_send);
    }
}