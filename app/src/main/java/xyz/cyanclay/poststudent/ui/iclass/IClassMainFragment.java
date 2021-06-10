package xyz.cyanclay.poststudent.ui.iclass;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.iclass.IClassCourse;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.ui.components.EFLReloadable;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;
import xyz.cyanclay.poststudent.util.Utils;

public class IClassMainFragment extends Fragment implements EFLReloadable, SwipeRefreshLayout.OnRefreshListener {

    //private IClassMainViewModel mViewModel;
    private View root;
    private boolean inited = false;
    private RecyclerView rv;
    private IClassCourseAdapter adapter;
    private int noticeNum;

    public IClassMainFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (root == null)
            root = inflater.inflate(R.layout.i_class_main_fragment, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!inited) {
            super.onViewCreated(view, savedInstanceState);
            rv = root.findViewById(R.id.recyclerViewIClassCourse);
            adapter = new IClassCourseAdapter(requireContext(), this);
            adapter.setLoading(true);
            rv.setAdapter(adapter);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.addItemDecoration(new DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL));
            fetchCourse(this, false);

            View gridAlarm = root.findViewById(R.id.gridIClassNotifications);
            gridAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(IClassMainFragment.this.requireActivity(), R.id.nav_host_fragment)
                            .navigate(R.id.action_to_nav_iclass_alarm);
                }
            });

            inited = true;
        }
    }

    /*
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(IClassMainViewModel.class);
        // TODO: Use the ViewModel
    }

 */

    private void setNoticeCount() {
        ((TextView) root.findViewById(R.id.textViewIClassAlarmCount)).setText(String.valueOf(noticeNum));
    }

    @Override
    public void reload() {
        fetchCourse(this, false);
    }

    @Override
    public void onRefresh() {
        fetchCourse(this, true);
    }

    static void fetchCourse(final IClassMainFragment fragment, boolean refresh) {
        final String[] message = new String[2];
        fragment.rv.removeAllViews();
        //fragment.srl.setRefreshing(true);
        fragment.adapter.setFailed(false);
        fragment.adapter.setLoading(true);
        fragment.adapter.notifyDataSetChanged();
        new TryAsyncTask<Void, Void, List<IClassCourse>>() {

            @Override
            protected List<IClassCourse> doInBackground(Void... voids) {
                try {
                    NetworkManager nm = Utils.getNetworkManager((MainActivity) fragment.requireActivity());
                    assert nm != null;

                    List<IClassCourse> list;
                    fragment.noticeNum = nm.iClassManager.parseActivityCount();
                    list = nm.iClassManager.parseCourseList();

                    return list;
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel(true);
                    message[0] = e.getMessage();
                    message[1] = e.toString();
                }
                return null;
            }

            @Override
            protected void cancelled(List<IClassCourse> a) {
                Snackbar.make(fragment.root, "发生了错误。" + message[0] + "///" + message[1], Snackbar.LENGTH_LONG);
                //fragment.srl.setRefreshing(false);
                fragment.adapter.setFailed(true);
            }

            @Override
            protected void postExecute(List<IClassCourse> list) {
                fragment.adapter.setList(list);
                fragment.adapter.setLoading(false);
                //fragment.srl.setRefreshing(false);
                fragment.setNoticeCount();
            }
        }.execute();

    }

}