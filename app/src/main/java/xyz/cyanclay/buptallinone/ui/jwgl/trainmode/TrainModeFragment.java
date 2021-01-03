package xyz.cyanclay.buptallinone.ui.jwgl.trainmode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.ClayManager;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.jwgl.trainmode.TrainModeCourseGroup;
import xyz.cyanclay.buptallinone.network.jwgl.trainmode.TrainModeCourseSubGroup;
import xyz.cyanclay.buptallinone.network.login.LoginException;
import xyz.cyanclay.buptallinone.ui.components.TryAsyncTask;
import xyz.cyanclay.buptallinone.util.Utils;

public class TrainModeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout srl;
    RecyclerView rv;
    TrainModeAdapter adapter;
    private View root;
    private Menu menu;
    private boolean inited = false;
    private boolean pickupMode = false;

    public TrainModeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        if (root == null)
            root = inflater.inflate(R.layout.fragment_train_mode, container, false);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!inited) {
            super.onViewCreated(view, savedInstanceState);
            rv = root.findViewById(R.id.rv_train_mode);
            srl = root.findViewById(R.id.srl_train_mode);

            adapter = new TrainModeAdapter(this);
            rv.setAdapter(adapter);

            srl.setOnRefreshListener(this);
            srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
            srl.setRefreshing(true);
            fetchTrainMode(this, false);

            inited = true;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_train_mode, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_upload_course_group) {
            //uploadCourseGroup(this);
            showThisTermOnly();
        } else if (id == R.id.action_refetch_group) {
            //fetchMinimumPoint(this);
            showThisTermOnly();
        } else if (id == R.id.action_enter_course_pickup) {
            enterPickupMode();
        } else if (id == R.id.action_exit_course_pickup) {
            exitPickupMode();
        } else if (id == R.id.action_show_this_term_only) {
            showThisTermOnly();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        fetchTrainMode(this, true);
    }

    void showThisTermOnly() {
        Snackbar.make(root, R.string.developing_, BaseTransientBottomBar.LENGTH_LONG).show();
    }

    void exitPickupMode() {
        menu.clear();

        this.requireActivity().getMenuInflater().inflate(R.menu.menu_train_mode, menu);
    }

    void enterPickupMode() {
        menu.clear();

        this.requireActivity().getMenuInflater().inflate(R.menu.pickup_course, menu);

    }

    static void fetchMinimumPoint(final TrainModeFragment fragment) {
        new TryAsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    ClayManager.getMinimumPoint(fragment.adapter.getList());
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void postExecute(Void aVoid) throws Exception {
                fragment.adapter.notifyDataSetChanged();
                Snackbar.make(fragment.root, R.string.fetched, BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        }.execute();
    }

    static void uploadCourseGroup(final TrainModeFragment fragment) {
        new TryAsyncTask<Void, Void, Map<TrainModeCourseSubGroup, ClayManager.NetCourseGroup>>() {
            @Override
            protected Map<TrainModeCourseSubGroup, ClayManager.NetCourseGroup> doInBackground(Void... voids) {
                try {
                    return ClayManager.doUpload(fragment.adapter.getList());
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            DialogInterface.OnClickListener dismisser = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };

            @Override
            protected void postExecute(final Map<TrainModeCourseSubGroup, ClayManager.NetCourseGroup> conflictMap) throws Exception {
                if (conflictMap.isEmpty()) {
                    Snackbar.make(fragment.root, R.string.uploaded, Snackbar.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                    builder.setMessage(R.string.conflict_train_group)
                            .setPositiveButton(R.string.go_watch, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder aBuilder = new AlertDialog.Builder(fragment.getContext());
                                    LayoutInflater inflater = LayoutInflater.from(aBuilder.getContext());
                                    View dialogView = inflater.inflate(R.layout.dialog_key_value, null);
                                    LinearLayout kvContainer = dialogView.findViewById(R.id.llKeyValueContainer);
                                    for (TrainModeCourseSubGroup subGroup : conflictMap.keySet()) {
                                        ClayManager.NetCourseGroup netGroup = conflictMap.get(subGroup);

                                        View nameView = inflater.inflate(R.layout.piece_kv_1to1, kvContainer, false);
                                        ((TextView) nameView.findViewById(R.id.textViewDetailKey)).setText(subGroup.getGroupName());
                                        ((TextView) nameView.findViewById(R.id.textViewDetailValue)).setText(netGroup.getGroupName());
                                        kvContainer.addView(nameView);

                                        View IDView = inflater.inflate(R.layout.piece_kv_1to1, kvContainer, false);
                                        ((TextView) IDView.findViewById(R.id.textViewDetailKey)).setText(subGroup.getGroupID());
                                        ((TextView) IDView.findViewById(R.id.textViewDetailValue)).setText(netGroup.getGroupID());
                                        kvContainer.addView(IDView);

                                        View pointView = inflater.inflate(R.layout.piece_kv_1to1, kvContainer, false);
                                        ((TextView) pointView.findViewById(R.id.textViewDetailKey)).setText(String.valueOf(subGroup.getMinimumPoint()));
                                        ((TextView) pointView.findViewById(R.id.textViewDetailValue)).setText(String.valueOf(netGroup.getMinimumPoint()));
                                        kvContainer.addView(pointView);
                                    }

                                    aBuilder.setView(dialogView);

                                    aBuilder.setPositiveButton(R.string.confirm, dismisser);
                                    aBuilder.show();

                                }
                            }).setNegativeButton(R.string.confirm, dismisser).show();
                }
            }

            @Override
            protected void cancelled() throws Exception {
                Snackbar.make(fragment.root, R.string.upload_failed, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }.execute();
    }

    static void fetchTrainMode(final TrainModeFragment fragment, final boolean force) {
        new TryAsyncTask<Void, Void, List<TrainModeCourseGroup>>() {
            LoginException e;

            @Override
            protected List<TrainModeCourseGroup> doInBackground(Void... voids) {
                NetworkManager nm = Utils.getNetworkManager((MainActivity) fragment.getActivity());

                if (nm == null) {
                    cancel(true);
                    return null;
                }

                try {
                    return nm.jwglManager.getTrainMode(force);
                } catch (LoginException e) {
                    this.e = e;
                    e.printStackTrace();
                    cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void postExecute(List<TrainModeCourseGroup> trainModeCourseGroups) throws Exception {
                fragment.adapter.setList(trainModeCourseGroups);
                fragment.srl.setRefreshing(false);
                //fetchMinimumPoint(fragment);
            }

            @Override
            protected void cancelled() throws Exception {
                super.cancelled();
            }
        }.execute();
    }
}