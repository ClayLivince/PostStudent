package xyz.cyanclay.buptallinone.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.ClayManager;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.info.Bus;
import xyz.cyanclay.buptallinone.ui.components.TryAsyncTask;
import xyz.cyanclay.buptallinone.util.Utils;

public class SchoolBusFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    View root;
    List<Bus> buses;
    RecyclerView rv;
    SwipeRefreshLayout srl;
    SchoolBusAdapter adapter;
    int destination = 0;

    static final int INIT_MIN_TIME = -100000;
    int minTime = INIT_MIN_TIME;

    public SchoolBusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_school_bus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.root = view;

        rv = root.findViewById(R.id.rvBusList);
        adapter = new SchoolBusAdapter(requireContext(), this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.addItemDecoration(new DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL));
        rv.setNestedScrollingEnabled(false);
        srl = root.findViewById(R.id.srlBus);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);

        taskFetchBus(this, destination);

        TextView tvDest = root.findViewById(R.id.textViewBusDestination);
        tvDest.setText(R.string.school_bus_main_campus);
        tvDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                if (destination == 1) {
                    destination = 0;
                    tv.setText(R.string.school_bus_main_campus);
                } else {
                    destination = 1;
                    tv.setText(R.string.school_bus_changping);
                }
                taskFetchBus(SchoolBusFragment.this, destination);
            }
        });

    }

    @Override
    public void onRefresh() {
        taskFetchBus(this, destination);
    }

    void setMinTime(int minTime) {
        TextView tv = root.findViewById(R.id.textViewBusHeaderTime);
        if (minTime > 0) {
            tv.setText(String.valueOf(minTime));
        } else {
            tv.setText("--");
        }
    }

    static void taskFetchBus(final SchoolBusFragment fragment, final int destination) {
        final String[] message = new String[2];
        fragment.rv.removeAllViews();
        fragment.srl.setRefreshing(true);
        fragment.adapter.setFailed(false);
        fragment.adapter.setLoading(true);
        fragment.adapter.notifyDataSetChanged();
        new TryAsyncTask<Void, Void, List<Bus>>() {
            @Override
            protected List<Bus> doInBackground(Void... voids) {
                try {
                    NetworkManager nm = Utils.getNetworkManager((MainActivity) fragment.requireActivity());
                    assert nm != null;
                    fragment.buses = ClayManager.getBus(nm);

                    LinkedList<Bus> list = new LinkedList<>();

                    int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

                    for (Bus bus : fragment.buses) {
                        if (today == bus.getWeekday()) {
                            if (bus.getDestination() == destination) {
                                list.add(bus);
                            }
                        }
                    }

                    findMin(list);

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
            protected void cancelled(List<Bus> a) {
                Snackbar.make(fragment.root, "发生了错误。" + message[0] + "///" + message[1], Snackbar.LENGTH_LONG);
                fragment.srl.setRefreshing(false);
                fragment.adapter.setFailed(true);
            }

            @Override
            protected void postExecute(List<Bus> buses) {
                fragment.adapter.setList(buses);
                fragment.adapter.setLoading(false);
                fragment.srl.setRefreshing(false);
                fragment.setMinTime(fragment.minTime);
            }

            private void findMin(List<Bus> buses) {
                Date date = new Date();
                String current = SchoolBusAdapter.timeFormatter.format(date);
                int min = INIT_MIN_TIME;
                for (Bus bus : buses) {

                    int[] lapse = Utils.calcTimeLapse(current, bus.getTime());
                    int calced;

                    if (min == INIT_MIN_TIME) {
                        min = 60 * lapse[0] + lapse[1];
                    }

                    if (lapse[0] > 0) {
                        calced = 60 * lapse[0] + lapse[1];
                        if (calced < fragment.minTime) {
                            min = calced;
                        }
                    } else if (lapse[0] == 0) {
                        if (lapse[1] > 0) {
                            calced = lapse[1];
                            if (calced < fragment.minTime) {
                                min = calced;
                            }
                        }
                    }
                }
                fragment.minTime = min;
            }
        }.execute();
    }
}