package xyz.cyanclay.poststudent.ui.iclass;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.entity.iclass.IClassActivities;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.ui.components.EFLReloadable;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;
import xyz.cyanclay.poststudent.util.Utils;

public class IClassNotificationFragment extends Fragment implements EFLReloadable, SwipeRefreshLayout.OnRefreshListener {

    //private IClassNotificationViewModel mViewModel;
    private View root;
    private boolean inited = false;
    private RecyclerView rv;
    private SwipeRefreshLayout srl;
    private IClassNoticeAdapter adapter;

    /**
     * Mandatory one
     */
    public IClassNotificationFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (root == null)
            root = inflater.inflate(R.layout.i_class_notification_fragment, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!inited) {
            super.onViewCreated(view, savedInstanceState);
            rv = root.findViewById(R.id.recyclerViewIClassNotice);
            srl = root.findViewById(R.id.srlIClassNotice);
            adapter = new IClassNoticeAdapter(requireContext(), this);
            adapter.setLoading(true);
            rv.setAdapter(adapter);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.addItemDecoration(new DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL));
            fetchActivity(this, false);
            inited = true;
        }
    }

    @Override
    public void onRefresh() {
        reload();
    }

    @Override
    public void reload() {
        fetchActivity(this, true);
    }

    static void fetchActivity(final IClassNotificationFragment fragment, boolean refresh) {
        final String[] message = new String[2];
        fragment.rv.removeAllViews();
        fragment.srl.setRefreshing(true);
        fragment.adapter.setFailed(false);
        fragment.adapter.setLoading(true);
        fragment.adapter.notifyDataSetChanged();
        new TryAsyncTask<Void, Void, IClassActivities>() {

            @Override
            protected IClassActivities doInBackground(Void... voids) {
                try {
                    NetworkManager nm = Utils.getNetworkManager((MainActivity) fragment.requireActivity());
                    assert nm != null;

                    IClassActivities list;
                    list = nm.iClassManager.parseActivity();

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
            protected void cancelled(IClassActivities a) {
                Snackbar.make(fragment.root, "发生了错误。" + message[0] + "///" + message[1], Snackbar.LENGTH_LONG);
                fragment.srl.setRefreshing(false);
                fragment.adapter.setFailed(true);
            }

            @Override
            protected void postExecute(IClassActivities list) {
                fragment.adapter.setList(list);
                fragment.adapter.setLoading(false);
                fragment.srl.setRefreshing(false);
            }
        }.execute();
    }
}