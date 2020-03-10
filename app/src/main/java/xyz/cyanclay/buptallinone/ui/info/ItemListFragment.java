package xyz.cyanclay.buptallinone.ui.info;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.InfoManager;
import xyz.cyanclay.buptallinone.network.NetworkManager;

import static xyz.cyanclay.buptallinone.network.InfoManager.INNER_CONTROL_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.InfoManager.NOTICE_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.InfoManager.PARTY_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.InfoManager.SCHOOL_NEWS;
import static xyz.cyanclay.buptallinone.network.InfoManager.SCHOOL_NOTICE;
import static xyz.cyanclay.buptallinone.network.InfoManager.SCHOOL_OVERTNESS;

public class ItemListFragment extends Fragment {

    private ItemListViewModel itemListViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        itemListViewModel =
                ViewModelProviders.of(this).get(ItemListViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_gallery, container, false);


        final NetworkManager nm = ((MainActivity) getActivity()).getNetworkManager();

        loadInfo(root, nm, getContext(), (MainActivity) getActivity(), false);

        ((SwipeRefreshLayout) root.findViewById(R.id.srlInfoList)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadInfo(root, nm, getContext(), (MainActivity) getActivity(), true);
            }
        });
        return root;
    }

    private static void loadInfo(final View root, final NetworkManager nm, final Context context, final MainActivity activity, final boolean isRefresh) {
        final ProgressBar pb = root.findViewById(R.id.progressBarInfo);
        pb.setVisibility(View.VISIBLE);
        final Map<Integer, LinearLayout> ref = new HashMap<Integer, LinearLayout>() {{
            put(SCHOOL_NOTICE, (LinearLayout) root.findViewById(R.id.NoticeContainer));
            put(SCHOOL_NEWS, (LinearLayout) root.findViewById(R.id.NewsContainer));
            put(PARTY_OVERTNESS, (LinearLayout) root.findViewById(R.id.PartyContainer));
            put(SCHOOL_OVERTNESS, (LinearLayout) root.findViewById(R.id.SchoolOvertContainer));
            put(INNER_CONTROL_OVERTNESS, (LinearLayout) root.findViewById(R.id.InnerControlContainer));
            put(NOTICE_OVERTNESS, (LinearLayout) root.findViewById(R.id.OvertnessContainer));
        }};

        new AsyncTask<Void, Void, InfoManager.InfoItems[]>() {
            @Override
            protected InfoManager.InfoItems[] doInBackground(Void... voids) {
                InfoManager.InfoItems[] items = new InfoManager.InfoItems[6];
                for (int i = 0; i <= NOTICE_OVERTNESS; i++) {
                    try {
                        items[i] = nm.infoManager.parseMainpage(i);
                    } catch (IOException e) {
                        cancel(true);
                        e.printStackTrace();
                    }
                }
                return items;
            }

            @Override
            protected void onPostExecute(final InfoManager.InfoItems[] infoItems) {
                super.onPostExecute(infoItems);
                for (int cate = 0; cate <= NOTICE_OVERTNESS; cate++) {
                    for (int i = 0; i <= 5; i++) {
                        final View item = View.inflate(context, R.layout.fragment_info_item, null);
                        item.setTag(infoItems[cate].get(i));
                        ((TextView) item.findViewById(R.id.textViewItemTitle)).setText(infoItems[cate].get(i).title);
                        ((TextView) item.findViewById(R.id.textViewItemTime)).setText(infoItems[cate].get(i).time);
                        final ItemDetailFragment idf = ItemDetailFragment.newInstance();
                        idf.setItem(infoItems[cate].get(i));

                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                activity.replaceFragment(idf);
                            }
                        });

                        Objects.requireNonNull(ref.get(cate)).addView(item);
                    }
                }

                if (isRefresh) {
                    ((SwipeRefreshLayout) root.findViewById(R.id.srlInfoList)).setRefreshing(false);
                    Snackbar.make(root, R.string.refreshed, Snackbar.LENGTH_LONG).show();
                }

                root.findViewById(R.id.layoutItemLists).setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);

            }

            @Override
            protected void onCancelled() {
                pb.setVisibility(View.GONE);
                root.findViewById(R.id.layoutItemLists).setVisibility(View.GONE);
                ((SwipeRefreshLayout) root.findViewById(R.id.srlInfoList)).setRefreshing(false);
                Snackbar.make(root, R.string.load_failed, Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }
}