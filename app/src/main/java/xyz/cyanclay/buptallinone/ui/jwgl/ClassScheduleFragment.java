package xyz.cyanclay.buptallinone.ui.jwgl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import xyz.cyanclay.buptallinone.R;

public class ClassScheduleFragment extends Fragment {

    private ClassScheduleViewModel mViewModel;

    public static ClassScheduleFragment newInstance() {
        return new ClassScheduleFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_schedule, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ClassScheduleViewModel.class);
        // TODO: Use the ViewModel
    }

}
