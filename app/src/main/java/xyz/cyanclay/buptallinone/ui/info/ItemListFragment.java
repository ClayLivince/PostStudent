package xyz.cyanclay.buptallinone.ui.info;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.info.InfoCategory;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItem;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItems;

import static xyz.cyanclay.buptallinone.network.info.InfoCategory.INNER_CONTROL_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.NOTICE_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.PARTY_OVERTNESS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.SCHOOL_NEWS;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.SCHOOL_NOTICE;
import static xyz.cyanclay.buptallinone.network.info.InfoCategory.SCHOOL_OVERTNESS;

public class ItemListFragment extends Fragment {

    private ItemListViewModel itemListViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        itemListViewModel =
                ViewModelProviders.of(this).get(ItemListViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_gallery, container, false);


        final NetworkManager nm = ((MainActivity) getActivity()).getNetworkManager();

        SwipeRefreshLayout srl = root.findViewById(R.id.srlInfoList);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        srl.setRefreshing(true);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadInfo(root, nm, getContext(), (MainActivity) getActivity(), true);
            }
        });

        loadInfo(root, nm, getContext(), (MainActivity) getActivity(), false);


        return root;
    }

    private static void loadInfo(final View root, final NetworkManager nm, final Context context, final MainActivity activity, final boolean isRefresh) {

        final Map<InfoCategory, LinearLayout> ref = new HashMap<InfoCategory, LinearLayout>() {{
            put(SCHOOL_NOTICE, (LinearLayout) root.findViewById(R.id.NoticeContainer));
            put(SCHOOL_NEWS, (LinearLayout) root.findViewById(R.id.NewsContainer));
            put(PARTY_OVERTNESS, (LinearLayout) root.findViewById(R.id.PartyContainer));
            put(SCHOOL_OVERTNESS, (LinearLayout) root.findViewById(R.id.SchoolOvertContainer));
            put(INNER_CONTROL_OVERTNESS, (LinearLayout) root.findViewById(R.id.InnerControlContainer));
            put(NOTICE_OVERTNESS, (LinearLayout) root.findViewById(R.id.OvertnessContainer));
        }};

        new AsyncTask<Void, Void, InfoItems[]>() {
            @Override
            protected InfoItems[] doInBackground(Void... voids) {
                InfoItems[] items = new InfoItems[6];
                for (InfoCategory cate : InfoCategory.values()) {
                    try {
                        items[cate.ordinal()] = nm.infoManager.parseMainpage(cate);
                    } catch (IOException e) {
                        cancel(true);
                        e.printStackTrace();
                    }
                }

                return items;
            }

            @Override
            protected void onPostExecute(final InfoItems[] infoItems) {
                super.onPostExecute(infoItems);
                for (InfoCategory cate : InfoCategory.values()) {

                    Objects.requireNonNull(ref.get(cate)).removeAllViews();

                    for (int i = 0; i <= 5; i++) {
                        final View itemView = View.inflate(context, R.layout.fragment_info_item, null);
                        final InfoItem item = infoItems[cate.ordinal()].get(i);
                        itemView.setTag(item);
                        ((TextView) itemView.findViewById(R.id.textViewItemTitle)).setText(item.title);
                        ((TextView) itemView.findViewById(R.id.textViewItemTime)).setText(item.time);

                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ItemDetailFragment idf = ItemDetailFragment.newInstance();
                                idf.setItem(item);
                                activity.addFragment(idf);
                            }
                        });

                        Objects.requireNonNull(ref.get(cate)).addView(itemView);
                    }
                }
                ((SwipeRefreshLayout) root.findViewById(R.id.srlInfoList)).setRefreshing(false);
                if (isRefresh) {
                    Snackbar.make(root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();
                }

                root.findViewById(R.id.layoutItemLists).setVisibility(View.VISIBLE);

            }

            @Override
            protected void onCancelled() {
                root.findViewById(R.id.layoutItemLists).setVisibility(View.GONE);
                ((SwipeRefreshLayout) root.findViewById(R.id.srlInfoList)).setRefreshing(false);
                Snackbar.make(root, R.string.load_failed, Snackbar.LENGTH_LONG).show();
            }
        }.execute();
    }
}